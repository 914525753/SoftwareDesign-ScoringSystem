// pages/login/login-wx/login-wx.js
// 获取应用实例
const app = getApp()
const domain = app.globalData.domain;

Page({
  data: {
    motto: "将使用微信账户进行登录",
    userInfo: {}
  },
  
  onLoad() {
    let userInfo = wx.getStorageSync('userInfo');
    
    // 设定页面问候语
    this.setData({
      motto: "将使用 " + userInfo.nickName + " 进行登录",
      userInfo: userInfo
    })
  },


  /*微信登录事件*/
  create_login_wx: function (e) {
    wx.login({
      success (res) {
        if (res.code) {
          
          let userInfo = wx.getStorageSync('userInfo');
          
          console.log(userInfo.nickName);
          console.log(res.code);

          // 发起微信登录请求
          wx.request({
            url: domain + "/LoginServlet2",
            data: {
              nickname: userInfo.nickName,
              code: res.code,
              method: "WeChatLogin"
            },
            method: "POST",
            header: {
              //'content-type': 'application/json' // 默认值
              'Content-Type': 'application/x-www-form-urlencoded;charset=utf-8'
            },
            
            // 处理返回数据
            success: function (res) {
    
              console.log("......Successful Request......");
              console.log(res.data);
          
              // Cookies
              if (res && res.header && res.header['Set-Cookie']) {
                wx.setStorageSync('JSESSIONID', res.header['Set-Cookie']); //保存Cookie到Storage
              }
              let cookie = wx.getStorageSync('JSESSIONID'); //取出Cookie
              let header = { 'Content-Type': 'application/x-www-form-urlencoded'};
              if (cookie) {
                  header.Cookie = cookie;
              }
              console.log(cookie)
          
              // 页面交互逻辑
              if (res.data == "11" || res.data == "12" || res.data == "3") {
                wx.showToast({
                  title: "微信登录成功",
                  duration: 1000
                })
                setTimeout(function () {
                  wx.reLaunch({
                    url: "/pages/index/index" // 关闭所有页面并打开主页
                  })
                }, 1000)
              }
              else if(res.data == "2" || res.data == "4") {
                wx.showToast({
                  title: "微信登录失败",
                  icon: 'none',
                  duration: 3000
                })
              }
              else if(res.data == "5") {
                wx.showToast({
                  title: "Code换取失败",
                  icon: 'none',
                  duration: 3000
                })
              }
          
            },
          })

        }
        else {
          console.log('无法获取临时登录凭证code' + res.errMsg);
        }
      }
    })
  },

})
