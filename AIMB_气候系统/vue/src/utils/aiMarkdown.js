/**
 * AI 回复的 Markdown / LaTeX 渲染
 *
 * 目标：模型输出的 Markdown（标题、列表、表格、粗体、链接、代码）与 LaTeX 公式
 *      （$$...$$、\[...\]、$...$、\(...\)）都能正常显示，而不是一堆纯文本符号。
 *
 * 实现要点：
 *  1) markdown-it 关闭 raw HTML（html:false），避免模型输出里的 <script> 之类被当 HTML 执行；
 *  2) 公式先被替换成占位符，等 markdown 渲染完再回填 KaTeX 的 HTML，
 *     这样公式里的 _ ^ \ 等符号不会被 markdown 语法干扰；
 *  3) 回填时跳过 <pre>/<code> 内部，代码块里的 $ 不会被误当公式；
 *  4) KaTeX 结果按公式文本缓存，流式逐字刷新时不会重复计算。
 */
import MarkdownIt from 'markdown-it'
import katex from 'katex'
import 'katex/dist/katex.min.css'

const md = new MarkdownIt({
  html: false,
  linkify: true,
  breaks: true,
  typographer: false
})

/* 链接一律新窗口打开 */
const defaultLinkOpen = md.renderer.rules.link_open
  || ((tokens, idx, options, env, self) => self.renderToken(tokens, idx, options))

md.renderer.rules.link_open = (tokens, idx, options, env, self) => {
  const token = tokens[idx]
  token.attrSet('target', '_blank')
  token.attrSet('rel', 'noopener noreferrer')
  return defaultLinkOpen(tokens, idx, options, env, self)
}

/* 图片懒加载 */
const defaultImage = md.renderer.rules.image
if (defaultImage) {
  md.renderer.rules.image = (tokens, idx, options, env, self) => {
    tokens[idx].attrSet('loading', 'lazy')
    return defaultImage(tokens, idx, options, env, self)
  }
}

/* ---------------- LaTeX 渲染 ---------------- */

const katexCache = new Map()

const escapeHtml = text => String(text)
  .replace(/&/g, '&amp;')
  .replace(/</g, '&lt;')
  .replace(/>/g, '&gt;')
  .replace(/"/g, '&quot;')

const renderTex = (tex, displayMode) => {
  const source = String(tex || '').trim()
  if (!source) return ''
  const key = (displayMode ? 'D:' : 'I:') + source
  if (katexCache.has(key)) return katexCache.get(key)
  let html
  try {
    html = katex.renderToString(source, {
      displayMode,
      throwOnError: false,
      strict: 'ignore',
      output: 'html'
    })
  } catch (error) {
    html = `<code>${escapeHtml(source)}</code>`
  }
  katexCache.set(key, html)
  return html
}

/* $$...$$ | \[...\] | \(...\) | $...$（按优先级从左到右匹配） */
const MATH_RE = /\$\$([\s\S]+?)\$\$|\\\[([\s\S]+?)\\\]|\\\(([\s\S]+?)\\\)|\$([^$\n]+?)\$/g

/* 行内 $ 的误判保护：纯数字（金额 / 温度）不当作公式 */
const isCurrencyLike = inner => /^[\d\s.,%+-]+$/.test(inner)

const extractMath = (text, store) => String(text).replace(
  MATH_RE,
  (match, dollars, bracket, paren, single) => {
    let tex = ''
    let display = false
    if (dollars !== undefined) {
      tex = dollars
      display = true
    } else if (bracket !== undefined) {
      tex = bracket
      display = true
    } else if (paren !== undefined) {
      tex = paren
    } else if (single !== undefined) {
      if (single.trim() !== single || isCurrencyLike(single)) return match
      tex = single
    } else {
      return match
    }
    const html = renderTex(tex, display)
    if (!html) return match
    store.push(html)
    return `%%AIMATH${store.length - 1}%%`
  }
)

/* 回填公式，跳过代码块内部 */
const restoreMath = (html, store) => String(html).replace(
  /(<pre[\s\S]*?<\/pre>)|(<code[\s\S]*?<\/code>)|%%AIMATH(\d+)%%/g,
  (match, pre, code, index) => {
    if (pre || code) return match
    const value = store[Number(index)]
    return value === undefined ? match : value
  }
)

/**
 * 把模型返回的文本渲染成可安全插入的 HTML
 * @param {string} text
 * @returns {string}
 */
export const renderAiMarkdown = text => {
  const source = String(text == null ? '' : text)
  if (!source.trim()) return ''
  const store = []
  const withPlaceholders = extractMath(source, store)
  const html = md.render(withPlaceholders)
  return restoreMath(html, store)
}

/** 纯文本模式（用户提问等不需要解析 Markdown 的场景） */
export const escapeAiText = text => escapeHtml(text)

export default renderAiMarkdown
