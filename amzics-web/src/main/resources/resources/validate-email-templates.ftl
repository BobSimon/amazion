<!DOCTYPE html>
<html lang="en" xmlns:th="http://www.thymeleaf.org">
<head lang="en">
    <meta charset="UTF-8" />
    <title>信息提示</title>
    <style>
        .main-con{
            background-color:#ECECEC; padding: 35px;
        }
        .main-tab{
            width: 600px; margin: 0px auto; text-align: left;
            position: relative; border-top-left-radius: 5px;
            border-top-right-radius: 5px; border-bottom-right-radius: 5px;
            border-bottom-left-radius: 5px; font-size: 14px; font-family:微软雅黑, 黑体;
            line-height: 1.5; box-shadow: #323e53 0px 0px 5px;
            border-collapse: collapse; background-position: initial initial;
            background-repeat: initial initial;background:#fff;
        }
        .main-th{
            height: 25px; line-height: 25px; padding: 15px 35px;
            border-bottom-width: 1px; border-bottom-style: solid;
            border-bottom-color: #323e53; background-color: #323e53;
            border-top-left-radius: 5px; border-top-right-radius: 5px;
            border-bottom-right-radius: 0px; border-bottom-left-radius: 0px;
        }
        .main-face{
            color: rgb(255, 255, 255);
        }
        .div-content{
            padding:25px 35px 40px; background-color:#fff;
        }
        .h2p{
            margin: 5px 0px;
        }
        .h2-font1{
            line-height: 20px;
        }
        .h2-font2{
            line-height: 22px;
        }
    </style>
</head>
<body>
<div class="main-con">
    <table class="main-tab" cellpadding="0" align="center">
        <tbody>
        <tr>
            <th class="main-th" valign="middle">
                <font face="微软雅黑" size="5" class="main-face">
                    <img width="100px" height="60px" src="http://p196x3e4d.bkt.clouddn.com/Amzics.png" alt="安知" />
                </font>
                <!--<font face="微软雅黑" size="5" class="main-face" style="">Amzics! （安知网）</font>-->
            </th>
        </tr>
        <tr>
            <td>
                <div class="div-content">
                    <h2 class="h2p">
                        <font color="#333333" class="h2-font1">
                            <font class="h2-font2" size="4">亲爱的 【${userName}】：</font>
                        </font>
                    </h2>
                    <p>您好！感谢您使用安知，请在30分钟内点击下方链接，${action}。</p>
                    <a href="${url}" style="color:#0f88eb;">点这里！点这里！</a>
                    <p>如果您并未发过此请求，则可能是因为其他用户在尝试注册时误输入了您的电子邮件地址而使您收到这封邮件，那么您可以放心的忽略此邮件，无需进一步采取任何操作。</p>
                    <p style="color:red;">(请注意，该电子邮件地址不接受回复邮件，要解决问题或了解您的帐户详情，请登录网站留言或咨询客服)</p>
                    <p align="right">此致</p>
                    <p align="right">安知团队敬上</p>
                    <p align="right">${time}</p>
                </div>
            </td>
        </tr>
        </tbody>
    </table>
</div>
</body>
</html>