// pages/create/create.js
var util = require('../../utils/util.js');
const app = getApp();
const domain = app.globalData.domain;
const date = new Date();
const years = [];
const months = [];
const days = [];
const hours = [];
const minutes = [];
//获取年
for (let i = date.getFullYear(); i <= date.getFullYear() + 5; i++) {
  years.push("" + i);
}
//获取月份
for (let i = 1; i <= 12; i++) {
  if (i < 10) {
    i = "0" + i;
  }
  months.push("" + i);
}
//获取日期
for (let i = 1; i <= 31; i++) {
  if (i < 10) {
    i = "0" + i;
  }
  days.push("" + i);
}
//获取小时
for (let i = 0; i < 24; i++) {
  if (i < 10) {
    i = "0" + i;
  }
  hours.push("" + i);
}
//获取分钟
for (let i = 0; i < 60; i++) {
  if (i < 10) {
    i = "0" + i;
  }
  minutes.push("" + i);
}
Page({
  start:function()
  {
    this.setData
    (
      {
        sflag: 1,
        eflag: 0,
      }
    )
  },

  end:function()
  {
    this.setData
    (
      {
        sflag: 0,
        eflag: 1,
      }
    )
  },

  formSubmit: function(e)
  {
    app.Check();
    var name = e.detail.value.name, desc = e.detail.value.desc, max = e.detail.value.max;
    var starttime = this.data.starttime, endtime = this.data.endtime;
    if(desc != "" && name != "" && max != "" && new Date(starttime) < new Date(endtime))
    {
      wx.request({
        url: domain + "/CreateContest",
        header: {
          "Content-Type": "application/x-www-form-urlencoded",
          "Cookie": wx.getStorageSync('JSESSIONID')
        },
        method: "POST",
        data: "name=" + name + "&desc=" + desc + "&starttime=" + starttime + "&endtime=" + endtime + "&max=" + max,
        success: function (res) {
          
          console.log(res.data);
          
          // 页面交互逻辑
          if (res.data == "1") {
            wx.showToast({
              title: '创建成功',
              icon: 'success',
              duration: 1000,
            })
            setTimeout(function () {
              wx.reLaunch({
                url: "/pages/index/index" // 关闭所有页面并打开主页
              })
            }, 1000)
          }
          else if (res.data == "3") {
            wx.showToast({
              title: '比赛已存在',
              icon: 'none',
              duration: 2000,
            })
          }
          else if (res.data == "4") {
            wx.showToast({
              title: '提交失败，请仔细检查相关的参数',
              icon: 'none',
              duration: 2000,
            })
          }

        },
        fail: function (res) {
          wx.showToast({
            title: '提交失败，请检查网络状况',
            icon: 'none',
            duration: 2000,
          });
        }
      });
    }
    else
    {
      wx.showToast({
        title: '提交失败，请仔细检查相关的参数',
        icon: 'none',
        duration: 2000,
      });
    } 
  },

  handleChange(e) {
    this.setData({
      value: e.detail.date
    })
  },

  /**
   * 页面的初始数据
   */
  data: {
    sflag: 0,
    eflag: 0, 
    starttime: '',
    endstarttime: '',
    multiArray: [years, months, days, hours, minutes],
    multiIndex: [0, date.getMonth(), date.getDate() - 1, date.getHours(), date.getMinutes()],
    choose_year: '',
  },
  onLoad: function() {
    //设置默认的年份
    this.setData({
      choose_year: this.data.multiArray[0][0]
    })
  },
  //获取时间日期
  bindMultiPickerChange: function(e) {
    // console.log('picker发送选择改变，携带值为', e.detail.value)
    this.setData({
      multiIndex: e.detail.value
    })
    const index = this.data.multiIndex;
    const year = this.data.multiArray[0][index[0]];
    const month = this.data.multiArray[1][index[1]];
    const day = this.data.multiArray[2][index[2]];
    const hour = this.data.multiArray[3][index[3]];
    const minute = this.data.multiArray[4][index[4]];
    // console.log(`${year}-${month}-${day}-${hour}-${minute}`);
    if(this.data.sflag == 1 && this.data.eflag == 0)
    {
      this.setData({
        starttime: year + '-' + month + '-' + day + ' ' + hour + ':' + minute + ":00"
      });
    }
    else if(this.data.sflag == 0 && this.data.eflag == 1)
    {
      this.setData({
        endtime: year + '-' + month + '-' + day + ' ' + hour + ':' + minute + ":00"
      });
    }

    // console.log(this.data.time);
  },
  //监听picker的滚动事件
  bindMultiPickerColumnChange: function(e) {
    //获取年份
    if (e.detail.column == 0) {
      let choose_year = this.data.multiArray[e.detail.column][e.detail.value];
      //console.log(choose_year);
      this.setData({
        choose_year
      })
    }
    //console.log('修改的列为', e.detail.column, '，值为', e.detail.value);
    if (e.detail.column == 1) {
      let num = parseInt(this.data.multiArray[e.detail.column][e.detail.value]);
      let temp = [];
      if (num == 1 || num == 3 || num == 5 || num == 7 || num == 8 || num == 10 || num == 12) { //判断31天的月份
        for (let i = 1; i <= 31; i++) {
          if (i < 10) {
            i = "0" + i;
          }
          temp.push("" + i);
        }
        this.setData({
          ['multiArray[2]']: temp
        });
      } else if (num == 4 || num == 6 || num == 9 || num == 11) { //判断30天的月份
        for (let i = 1; i <= 30; i++) {
          if (i < 10) {
            i = "0" + i;
          }
          temp.push("" + i);
        }
        this.setData({
          ['multiArray[2]']: temp
        });
      } else if (num == 2) { //判断2月份天数
        let year = parseInt(this.data.choose_year);
        //console.log(year);
        if (((year % 400 == 0) || (year % 100 != 0)) && (year % 4 == 0)) {
          for (let i = 1; i <= 29; i++) {
            if (i < 10) {
              i = "0" + i;
            }
            temp.push("" + i);
          }
          this.setData({
            ['multiArray[2]']: temp
          });
        } else {
          for (let i = 1; i <= 28; i++) {
            if (i < 10) {
              i = "0" + i;
            }
            temp.push("" + i);
          }
          this.setData({
            ['multiArray[2]']: temp
          });
        }
      }
      //console.log(this.data.multiArray[2]);
    }
    var data = {
      multiArray: this.data.multiArray,
      multiIndex: this.data.multiIndex
    };
    data.multiIndex[e.detail.column] = e.detail.value;
    this.setData(data);
  },
})