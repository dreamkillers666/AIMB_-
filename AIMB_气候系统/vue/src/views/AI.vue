<template>
  <!-- 1. 使用我们统一的深色主题“毛玻璃”卡片作为根元素 -->
  <div class="dark-theme-card chat-page-wrapper">
    <div class="content-wrapper">
      <h2 class="content-title">DeepSeek AI</h2>

      <!-- 2. 改造后的聊天容器 -->
      <div class="chat-container">
        <!-- 聊天记录区域 -->
        <div class="chat-messages" ref="chatMessages">
          <!-- 欢迎语/初始状态 -->
          <div v-if="finalChat.length === 0" class="welcome-message">
            <i class="el-icon-chat-dot-round welcome-icon"></i>
            <p>DeepSeek 乐意为您解答困惑</p>
          </div>

          <!-- 聊天消息 -->
          <div v-for="(chatContent, index) in finalChat" :key="index" class="message-wrapper">
            <!-- 用户消息 -->
            <div class="message user-message">
              <div class="message-content">{{ chatContent.user }}</div>
            </div>
            <!-- AI 消息 -->
            <div class="message ai-message">
              <div class="message-content" v-html="formatAiMessage(chatContent.ai)"></div>
            </div>
          </div>
        </div>

        <!-- 输入区域 -->
        <div class="chat-input">
          <el-row :gutter="10" type="flex" align="middle">
            <el-col :span="20">
              <el-input
                  type="textarea"
                  placeholder="请输入需要我解答的问题！(Enter 发送, Ctrl+Enter 换行)"
                  v-model="userInput"
                  maxlength="500"
                  show-word-limit
                  :autosize="{ minRows: 2, maxRows: 4 }"
                  resize="none"
                  class="chat-input-textarea"
                  @keydown.native.enter.exact.prevent="start"
                  @keydown.native.enter.ctrl.prevent="userInput += '\n'"
              />
            </el-col>
            <el-col :span="4">
              <el-button @click="start" :loading="loading" :disabled="loading" class="chat-send-button">
                {{ loading ? '思考中' : '发送' }}
              </el-button>
            </el-col>
          </el-row>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';
import nlp from "compromise";

export default {
  name: "DSAI", // 修正组件名
  data() {
    return {
      userInput: '',
      finalChat: [],
      loading: false,
      landQueryRegEx: /(台风|热带气旋).*(登陆|登录).*(查询|情况|信息)?/,
      currentTyphoonQueryRegEx: /(当前|正在发生|目前|此刻|此时此刻|现在).*(台风|热带气旋).*(查询|情况|信息)?/
    };
  },
  methods: {
    formatAiMessage(message) {
      if (!message) return '';
      let formatted = message.replace(/\n/g, '<br>');
      formatted = formatted.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>');
      return formatted;
    },

    async start() {
      const userMessage = this.userInput.trim();
      if (!userMessage) {
        this.$message.warning('请输入问题！');
        return;
      }

      this.loading = true;
      this.finalChat.push({ user: userMessage, ai: '思考中...' });
      this.userInput = '';
      this.scrollToBottom();

      try {
        let aiResponse;
        if (this.landQueryRegEx.test(userMessage) || this.currentTyphoonQueryRegEx.test(userMessage)) {
          aiResponse = await this.getTyphoonData(userMessage);
        } else {
          aiResponse = await this.callDeepSeekApi(userMessage);
        }
        this.finalChat[this.finalChat.length - 1].ai = aiResponse;
      } catch (error) {
        this.finalChat[this.finalChat.length - 1].ai = "抱歉，请求失败，请稍后再试。";
        this.$message.error('API 请求失败，请查看控制台获取详情。');
        console.error("Request failed:", error);
      } finally {
        this.loading = false;
        this.scrollToBottom();
      }
    },

    async getTyphoonData(userMessage) {
      const response = await axios.get('https://tf.istrongcloud.com/data/complex/path.json');
      const currentTyphoons = response.data.filter(t => t.is_current === 1);
      const landTyphoons = response.data.filter(t => t.land && t.land.length > 0);
      let typhoonInfo = '';

      if (this.landQueryRegEx.test(userMessage) && landTyphoons.length > 0) {
        landTyphoons.forEach(t => {
          const landPositions = t.land.map(l => l.position).join(', ');
          typhoonInfo += `台风 **${t.name} (${t.ename})** (编号: ${t.tfbh}) 的登陆信息：\n- **降临位置**: ${landPositions}\n- **起止时间**: ${t.begin_time} 至 ${t.end_time}\n\n`;
        });
      }

      if (this.currentTyphoonQueryRegEx.test(userMessage) && currentTyphoons.length > 0) {
        currentTyphoons.forEach(t => {
          typhoonInfo += `当前活跃的台风 **${t.name} (${t.ename})** (编号: ${t.tfbh})：\n- **状态**: 当前活跃\n- **起止时间**: ${t.begin_time} 至 ${t.end_time}\n\n`;
        });
      }

      return typhoonInfo || '未找到相关的台风登陆或当前活跃台风的数据。';
    },

    async callDeepSeekApi(prompt) {
      const url = 'https://api.deepseek.com/chat/completions';
      const apiKey = 'sk-0a832b12dc844c9f81341c83c4965439';

      const response = await axios.post(url,
          { model: "deepseek-chat", messages: [{ "role": "user", "content": prompt }] },
          { headers: { "Authorization": `Bearer ${apiKey}`, "Content-Type": "application/json" } }
      );
      return response.data.choices[0].message.content;
    },

    scrollToBottom() {
      this.$nextTick(() => {
        const chatMessages = this.$refs.chatMessages;
        if (chatMessages) {
          chatMessages.scrollTop = chatMessages.scrollHeight;
        }
      });
    }
  }
};
</script>

<style scoped>
/* --- 统一的深色主题样式 --- */

/* 主卡片容器 */
.dark-theme-card { background-color: rgba(20, 30, 50, 0.75); backdrop-filter: blur(12px); border: 1px solid rgba(100, 116, 139, 0.5); box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2); border-radius: 16px; height: calc(100vh - 100px); }
::v-deep .el-card__body { padding: 24px 32px; height: 100%; box-sizing: border-box; }

.content-wrapper { height: 100%; display: flex; flex-direction: column; }
.content-title { color: #58a6ff; margin-bottom: 24px; text-align: center; flex-shrink: 0; }

/* 主聊天容器 */
.chat-container { flex: 1; display: flex; flex-direction: column; background-color: rgba(0, 0, 0, 0.2); border: 1px solid rgba(100, 116, 139, 0.3); border-radius: 12px; overflow: hidden; }

/* 聊天记录区域 */
.chat-messages { flex: 1; overflow-y: auto; padding: 20px; }

/* 欢迎语 */
.welcome-message { display: flex; flex-direction: column; align-items: center; justify-content: center; height: 100%; color: #94a3b8; text-align: center; }
.welcome-icon { font-size: 4em; margin-bottom: 16px; }
.welcome-message p { font-size: 1.2em; font-weight: bold; }
.welcome-message span { font-size: 0.9em; }

/* 消息气泡 */
.message-wrapper { margin-bottom: 20px; }
.message { display: flex; margin-bottom: 10px; max-width: 80%; }
.user-message { justify-content: flex-end; margin-left: auto; }
.user-message .message-content { background-color: #58a6ff; color: #fff; border-radius: 12px 12px 0 12px; }
.ai-message { justify-content: flex-start; }
.ai-message .message-content { background-color: rgba(0, 0, 0, 0.3); color: #e0e7ff; border-radius: 12px 12px 12px 0; }
.message-content { padding: 12px 18px; word-wrap: break-word; line-height: 1.6; font-size: 16px; }
::v-deep .message-content strong { color: #67e8f9; }

/* 输入区域 */
.chat-input { padding: 15px 20px; border-top: 1px solid rgba(100, 116, 139, 0.3); background-color: rgba(0, 0, 0, 0.2); flex-shrink: 0; }
::v-deep .chat-input-textarea .el-textarea__inner { background-color: rgba(0, 0, 0, 0.3) !important; border: 1px solid rgba(100, 116, 139, 0.5) !important; border-radius: 8px; color: #e0e7ff !important; font-size: 16px; }
::v-deep .chat-input-textarea .el-input__count { background-color: transparent !important; color: #94a3b8; }

/* 发送按钮 */
.chat-send-button { width: 100%; height: 100%; min-height: 54px; font-size: 16px; font-weight: bold; border-radius: 8px; background-color: #58a6ff !important; border-color: #58a6ff !important; color: #fff !important; }
.chat-send-button.is-loading { background-color: rgba(88, 166, 255, 0.5) !important; border-color: rgba(88, 166, 255, 0.5) !important; }

/* 自定义滚动条 */
.chat-messages::-webkit-scrollbar { width: 8px; }
.chat-messages::-webkit-scrollbar-track { background: transparent; }
.chat-messages::-webkit-scrollbar-thumb { background-color: rgba(148, 163, 184, 0.3); border-radius: 4px; }
.chat-messages::-webkit-scrollbar-thumb:hover { background-color: rgba(148, 163, 184, 0.5); }
</style>