import SockJS from 'sockjs-client'
import { Client } from '@stomp/stompjs'
import { ElNotification } from 'element-plus'

/**
 * WebSocket连接管理器
 */
class WebSocketManager {
  constructor() {
    this.client = null
    this.connected = false
    this.userId = null
    this.messageHandlers = []
  }

  /**
   * 连接WebSocket
   */
  connect(userId) {
    if (this.connected) {
      console.log('WebSocket已连接')
      return
    }

    this.userId = userId

    // 创建SockJS连接
    const socket = new SockJS('http://localhost:8084/ws/notification')

    // 创建STOMP客户端
    this.client = new Client({
      webSocketFactory: () => socket,
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      debug: (str) => {
        console.log('STOMP Debug:', str)
      },
      onConnect: () => {
        console.log('WebSocket连接成功')
        this.connected = true

        // 订阅用户私有队列
        this.client.subscribe(`/user/${userId}/queue/notification`, (message) => {
          console.log('收到通知:', message.body)
          const notification = JSON.parse(message.body)
          this.handleNotification(notification)
        })

        // 订阅广播主题
        this.client.subscribe('/topic/notification', (message) => {
          console.log('收到广播通知:', message.body)
          const notification = JSON.parse(message.body)
          this.handleNotification(notification)
        })
      },
      onDisconnect: () => {
        console.log('WebSocket断开连接')
        this.connected = false
      },
      onStompError: (frame) => {
        console.error('STOMP错误:', frame)
      }
    })

    // 激活连接
    this.client.activate()
  }

  /**
   * 断开连接
   */
  disconnect() {
    if (this.client) {
      this.client.deactivate()
      this.connected = false
      console.log('WebSocket已断开')
    }
  }

  /**
   * 处理收到的通知
   */
  handleNotification(notification) {
    // 显示桌面通知
    this.showDesktopNotification(notification)

    // 调用所有注册的消息处理器
    this.messageHandlers.forEach(handler => {
      try {
        handler(notification)
      } catch (error) {
        console.error('消息处理器执行失败:', error)
      }
    })
  }

  /**
   * 显示桌面通知
   */
  showDesktopNotification(notification) {
    const typeMap = {
      announcement: 'info',
      homework: 'warning',
      exam: 'error',
      cancel: 'warning'
    }

    ElNotification({
      title: notification.title,
      message: notification.content,
      type: typeMap[notification.notificationType] || 'info',
      duration: 5000,
      position: 'top-right'
    })
  }

  /**
   * 注册消息处理器
   */
  onMessage(handler) {
    this.messageHandlers.push(handler)
  }

  /**
   * 移除消息处理器
   */
  offMessage(handler) {
    const index = this.messageHandlers.indexOf(handler)
    if (index > -1) {
      this.messageHandlers.splice(index, 1)
    }
  }

  /**
   * 检查是否已连接
   */
  isConnected() {
    return this.connected
  }
}

// 导出单例
export default new WebSocketManager()
