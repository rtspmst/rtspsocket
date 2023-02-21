    # LM Android Client
        
    * Preview RTSP video use EasyPlyer
    * Auido from socket on TCP
    * Video from RTSP 
    * Encapsulation MP4 use audio form socket and video from RTSP
    
    Ctrl + O            重写/覆盖 父类的方法           Override methods   
    Ctrl + w            选择联续的代码块，比如一个方法内的所有代码           Select successively increasing code blocks   
    Ctrl + NumPad+/-    展开/折叠代码块     Expand/collapse code block   
    Ctrl + Shift + NumPad+  展开所有代码 Expand all   
    
    2020 08 06   更改 全模式下 降噪一二三四
    
    2020 09     保存测试准星坐标
    
    2020 10 12 更改发送命令 三个线程发送三条命令 改为 一个线程分三次发送三个命令
    
    2020 10 12 修改 降噪四 也可以选择声源质量 
    
    2020 10 13 降噪方法 添加声源质量选择三级
    
    2020 11 03 添加webrtc_agc pcm音量增益控制 音量按键增10控制本地音量增大 
               右半屏滑动2发送指令机器声音增大
               
    2020 11 10 修改录音时 声音随着音量加减键调节而增大	 
               修改 当距离发生变化超过两米时 自动对焦一次 每五秒刷新一次距离 长按自动对焦实现       
               
    2020 12 11 修改MainAcvity 右侧滑动调节音量
    
    2020 12 14 修改开机日志去除前台透传通信连接 修改设备端关机后更新日志信息不准确bug 
               右半屏滑动调节音量由百分之二改为百分之五 最小百分之十
               
    2020 12 28 添加首次打开app自动设置准星到屏幕中心
    
    2021 01 19 使用viewbing替换控件初始化
    
    2021 01 22 添加创建顺风耳文件夹，
               修改引用范宏闯的悬浮按钮私人库改为本地库
               
    2021 01 25 添加获取步进电机前进步数 5579
    
    2021 01 28 1185调整光斑时 添加方向键 实现微调
    
    2021 02 02 添加三分钟无操作 自动息屏进入省电模式
    
    2021 02 24 添加密码9999进入设置页面 实现无需重新安装apk进行中英文切换功能
    
    2021 02 26 添加密码9999进入设置页面 不重新安装新apk的情况下实现按钮显示隐藏（有线模式 晃动模式 NONE 降噪方法）
    
    2021 03 08 优化有线模式和无线模式切换时，自动更改WiFi状态， 添加有线模式和无线模式区别标志
               修改英文版本晃动模式和正常模式的bug
               
    2021 03 23 去除录像回放页面ViewBing 与EmptyView冲突导致白屏问题    
           
    2021 03 24 修改录像回放页面不展示拍照保存下来的照片的bug 添加图片点击事件可对图片进行放大缩小
    
    2021 03 31 添加电动云台控制 添加一键自检功能（折叠屏）
    
    2021 04 14 添加屏幕亮度记录
    
    2021 06 14 修改晃动模式 NONE 音量调节发送命令方式
    
    2021 06 14 添加点击声音按钮 三秒内响应隐藏按钮的三次连点
    
    2021 11 10 目标版本30改为29 解决红米note10pro不能录像不能拍照的问题
    
    2021 11 16 开始音量控制mJsonHandle（哈萨克斯坦） 48台以后改为mUartHandle 音量控制区分 
    
    2021 12 10 解决返修设备录像回放导致app崩溃问题
    
    2021 12 14 重新定义为二维调整架的快调功能 === 修改以后四条命令 ===
    
    2022 03 04 初始化 模式切换 声音设置 分别放在 mJsonHandle_6802 mUartHandle_6804 来完成
               需要打开一个 屏蔽一个 48台应该在6804主控板修改
               
    2022 03 31 优化线程池 修改计时器 云台添加快慢调节 整合长按点击事件 修改长按点击事件计时器
                修改自动对焦命令 侧菜单栏禁止手势滑动
                
--------------------------------------------------------------------------------------------------------------------

    2022 04 14 修改适配平板屏幕尺寸 修改音量调节
    
    2022 06 14 修改主控板断开连接不需要重启app
    
    2022 06 21 关闭其他音乐播放器 优化录像时间 按home键以后停止录像 双击退出时正在录像时提示 RTSP_KEY位置变动
    
    2022 06 21 录像回放页面点击item直接播放视频 HOME键监听关闭录像 优化录像时间
        
    2022 06 30 控制权展示 有视频没声音 
               录像回放区分日期和时间
               连接后再断网设置不可录像
    
    2022 06 30 自测无问题版本
               已保存源码 安装包
               平板华为matepad11 
               瑞安 = 匈牙利


    准星对准光斑时添加方向键盘实现微调，避免手指抬起时造成准星滑动问题
    新设备首次安装app时自动创建ShunFengEr,movie,pcm,picture文件夹（原来手动创建）
    性能优化，使用viewbing替换控件初始化
    顺风耳App英文版与现有版本合并，避免更改代码后没有同步问题
    修改原来范工使用的悬浮按钮私人库改为本地库，避免个人链接不可用时，项目无法运行
    处理新手机获取root权限问题
    添加三分钟无操作 自动息屏进入省电模式

    //写入音频
    //写入视频
    
    //============
    //   =   =   =
    //====   =====
    //           =
    //====       =
    //   =       =
    //============
    //           =
    //============

    parameter CMD_NORMAL_MODE 	    =  5A A5 0A 02 01 71 02 00 00 DD	；  正常模式    
    parameter CMD_MOVING_MODE 	    =  5A A5 0A 02 01 72 02 00 00 DD	：  晃动模式
    parameter CMD_HIGHPOWER_MODE 	=  5A A5 0A 02 01 73 02 00 00 DD	：  高功率
    parameter CMD_LOWPOWER_MODE 	=  5A A5 0A 02 01 74 02 00 00 DD	：  低功率
    parameter CMD_VOLUME_SET 	    =  5A A5 0A 02 01 75 02 01 28 DD	：  音量设置  （max volume)

      case CV.ADJUST_AUTO_FOCUS_START:

                //调整自动对焦开始
                msg.what = CV.MSG_ADJUST_AUTO_FOCUS_UPDATE;
                // rssi1
                msg.arg1 = ((data_handle_buf[CV.FRAME_DATA_START_FIELD + 2] << 8) & 0x0000FF00) | data_handle_buf[CV.FRAME_DATA_START_FIELD + 3];
                // rssi2
                msg.arg2 = ((data_handle_buf[CV.FRAME_DATA_START_FIELD + 4] << 8) & 0x0000FF00) | data_handle_buf[CV.FRAME_DATA_START_FIELD + 5];
                mHandler.sendMessage(msg);
	

    armeabiv-v7a: 第7代及以上的 ARM 处理器。2011年15月以后的生产的大部分Android设备都使用它.
    arm64-v8a: 第8代、64位ARM处理器，很少设备，三星 Galaxy S6是其中之一。
    armeabi: 第5代、第6代的ARM处理器，早期的手机用的比较多。
    x86: 平板、模拟器用得比较多。
    x86_64: 64位的平板。


        externalNativeBuild {
            cmake {
    //                cppFlags "-std=c++11 -frtti -fexceptions"
    //                abiFilters /*'armeabi', 'mips',*/ 'armeabi-v7a', 'arm64-v8a', 'x86', 'x86_64'//NDK 17及以上不再支持ABIs [mips64, armeabi, mips]

    //             'arm64-v8a'9.12添加 clean项目以后找不到自己的so

                abiFilters 'arm64-v8a','armeabi-v7a'//NDK 17及以上不再支持ABIs [mips64, armeabi, mips]
            }
        }


    与运算：&
    两者都为1则为1，否则为0
    1&1 = 1，1&0 = 0, 0&1 = 0，0 & 0 = 0
    或运算：|
    两者都为0为0，否则为1
    1 | 1 = 1,       1 | 0= 1,    0 | 1 = 1,    0 | 0 = 0
    非运算：~
    1取0    0 取1
    ~1 = 0       ~ 0 = 1
    ~（1001） = 0110
    异或运算^
    两者相等为0，不等为1
    1^1 = 0       1^0= 1        0^1 = 0        0^0 = 0
    位运算操作符：
    有符号：<<左移   >>右移 
    无符号: >>>右移  忽略符号位，空位都以0补齐，不存在无符号左移，因为左移是右边补0，有符号和无符号都一样，所以没必要



    //调整自动对焦开始
    //十六进制转十进制
    msg.arg1 = ((data_handle_buf[7] << 8) & 0x0000FF00) | data_handle_buf[8];
    msg.arg2 = ((data_handle_buf[9] << 8) & 0x0000FF00) | data_handle_buf[10];
    
    //十进制转十六进制参考设置声音
        private void setTx2Volume(int sdsd1) {
            byte[] data;
            data = new byte[4];
            data[0] = 0x75;
            data[1] = 0x02;
            data[2] = (byte) ((sdsd1 & 0xFF00) >> 8);
            data[3] = (byte) (sdsd1 & 0x00FF);
            //设置音量 48台
            mUartHandle_6804.sendWriteCmd_WithAttrIDAndData(data, data.length);
        }

 
序号	方法&描述
1	getSurfaceTexture()
This method returns the SurfaceTexture used by this view.
2	getBitmap(int width, int height)
This method returns Returns a Bitmap representation of the content of the associated surface texture.
3	getTransform(Matrix transform)
This method returns the transform associated with this texture view.
4	isOpaque()
This method indicates whether this View is opaque.
5	lockCanvas()
This method start editing the pixels in the surface
6	setOpaque(boolean opaque)
This method indicates whether the content of this TextureView is opaque.
7	setTransform(Matrix transform)
This method sets the transform to associate with this texture view.
8	unlockCanvasAndPost(Canvas canvas)
This method finish editing pixels in the surface.

2022 03 11 本周 顺风耳_IP项目计时器及延时任务改用线程池管理（开机日志 自动息屏等），去除无用的子线程
           下周 设置位置功能：自定义控件 可拖拽矩形改变矩形大小流畅度进行优化，并在手机屏幕标注各个顶点X，Y轴数值