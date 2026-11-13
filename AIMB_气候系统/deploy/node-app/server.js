/**
 * AIMB_ 气候系统 · 单端口托管服务
 * - 静态托管前端 dist（SPA 回退）
 * - 同源代理 /ai-gateway/* → https://api.gs88.shop/v1/*（SSE 流式透传）
 * - API Key 只在服务端（环境变量 AI_GATEWAY_KEY 覆盖）
 */
const http = require('http');
const https = require('https');
const fs = require('fs');
const path = require('path');

const PORT = process.env.PORT || 3000;
const PUBLIC_DIR = path.join(__dirname, 'public');
const UPSTREAM_HOST = 'api.gs88.shop';
const API_KEY = process.env.AI_GATEWAY_KEY || 'sk-14ea482f5c106ee664816eb2d2c5cc1c9640139aa8d8727826ec66be5364106e';

const MIME = {
  '.html': 'text/html; charset=utf-8',
  '.js': 'application/javascript; charset=utf-8',
  '.css': 'text/css; charset=utf-8',
  '.json': 'application/json; charset=utf-8',
  '.png': 'image/png',
  '.jpg': 'image/jpeg',
  '.jpeg': 'image/jpeg',
  '.gif': 'image/gif',
  '.svg': 'image/svg+xml',
  '.ico': 'image/x-icon',
  '.woff': 'font/woff',
  '.woff2': 'font/woff2',
  '.ttf': 'font/ttf',
  '.eot': 'application/vnd.ms-fontobject',
  '.map': 'application/json',
  '.txt': 'text/plain; charset=utf-8',
  '.webp': 'image/webp',
  '.mp4': 'video/mp4',
  '.nc': 'application/octet-stream'
};

const HOP_HEADERS = ['transfer-encoding', 'connection', 'keep-alive'];

function proxy(req, res) {
  const targetPath = (req.url.replace(/^\/ai-gateway/, '') || '/');
  const headers = Object.assign({}, req.headers);
  delete headers.host;
  delete headers['content-length'];
  HOP_HEADERS.forEach(h => delete headers[h]);
  headers.host = UPSTREAM_HOST;
  headers.authorization = 'Bearer ' + API_KEY;

  const upstream = https.request(
    { hostname: UPSTREAM_HOST, port: 443, path: targetPath, method: req.method, headers },
    upRes => {
      const outHeaders = Object.assign({}, upRes.headers);
      HOP_HEADERS.forEach(h => delete outHeaders[h]);
      outHeaders['cache-control'] = outHeaders['cache-control'] || 'no-store';
      res.writeHead(upRes.statusCode || 502, outHeaders);
      upRes.pipe(res);
    }
  );
  upstream.setTimeout(300000, () => upstream.destroy(new Error('upstream timeout')));
  upstream.on('error', err => {
    if (!res.headersSent) {
      res.writeHead(502, { 'Content-Type': 'application/json; charset=utf-8', 'Cache-Control': 'no-store' });
    }
    res.end(JSON.stringify({ error: { message: 'AI 网关转发失败: ' + err.message } }));
  });
  req.pipe(upstream);
}

function serveStatic(req, res) {
  let pathname = '/';
  try {
    pathname = decodeURIComponent(new URL(req.url, 'http://localhost').pathname);
  } catch (e) {
    pathname = '/';
  }
  if (pathname.endsWith('/')) pathname += 'index.html';

  let filePath = path.normalize(path.join(PUBLIC_DIR, pathname));
  if (!filePath.startsWith(PUBLIC_DIR)) {
    res.writeHead(403, { 'Content-Type': 'text/plain' });
    res.end('Forbidden');
    return;
  }

  fs.stat(filePath, (err, stat) => {
    if (err || !stat.isFile()) {
      filePath = path.join(PUBLIC_DIR, 'index.html');
    }
    const ext = path.extname(filePath).toLowerCase();
    const isHtml = ext === '.html';
    res.writeHead(200, {
      'Content-Type': MIME[ext] || 'application/octet-stream',
      'Cache-Control': isHtml ? 'no-cache' : 'public, max-age=3600'
    });
    fs.createReadStream(filePath).pipe(res);
  });
}

const server = http.createServer((req, res) => {
  if (req.url.startsWith('/ai-gateway/')) {
    proxy(req, res);
    return;
  }
  if (req.url === '/healthz') {
    res.writeHead(200, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify({ ok: true }));
    return;
  }
  serveStatic(req, res);
});

server.listen(PORT, () => {
  console.log('AIMB web server listening on ' + PORT);
});
