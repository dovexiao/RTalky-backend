package services

import (
	"fmt"
	"gopkg.in/gomail.v2"
)

var EmailDialer *gomail.Dialer

func sendEmail(sourceUser, targetUser, subject, bodyType, body string) error {
	m := gomail.NewMessage()

	m.SetHeader("From", sourceUser)
	m.SetHeader("To", targetUser)
	m.SetHeader("Subject", subject)
	m.SetBody(bodyType, body)

	if err := EmailDialer.DialAndSend(m); err != nil {
		return err
	}

	return nil
}

func SendImageCaptchaEmail(imageBase64, targetUser string) error {
	htmlBody := fmt.Sprintf(`
<html>
  <body style="font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen,
    Ubuntu, Cantarell, 'Open Sans', 'Helvetica Neue', sans-serif; background:#f4f6f8; margin:0; padding:0;">
    <table align="center" width="600" cellpadding="0" cellspacing="0" style="background:#ffffff; margin:40px auto; border-radius:8px; box-shadow: 0 4px 8px rgba(0,0,0,0.1);">
      <tr>
        <td style="padding:30px; text-align:center; border-bottom:1px solid #eee;">
          <h2 style="color:#333333; margin:0;">验证码验证</h2>
          <p style="color:#666666; font-size:14px; margin-top:8px;">请使用下面的验证码完成验证</p>
        </td>
      </tr>
      <tr>
        <td style="padding:40px 0; text-align:center;">
          <img src="%s" alt="验证码" style="width:160px; height:60px; border:1px solid #ddd; border-radius:4px;"/>
        </td>
      </tr>
      <tr>
        <td style="padding:0 30px 40px; text-align:center; font-size:14px; color:#555555; line-height:1.5;">
          如果您没有请求此验证码，请忽略本邮件。<br/>
          感谢您的支持！
        </td>
      </tr>
      <tr>
        <td style="background:#f0f0f0; padding:20px 30px; text-align:center; font-size:12px; color:#999999;">
          &copy; 2025 SReader。保留所有权利。
        </td>
      </tr>
    </table>
  </body>
</html>`, imageBase64)

	err := sendEmail(EmailDialer.Username, targetUser, "Email地址验证", "text/html", htmlBody)

	return err
}

func SendTextCaptchaEmail(captchaCode, targetUser string) error {
	htmlBody := fmt.Sprintf(`
<html>
  <body style="font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen,
    Ubuntu, Cantarell, 'Open Sans', 'Helvetica Neue', sans-serif; background:#f4f6f8; margin:0; padding:0;">
    <table align="center" width="600" cellpadding="0" cellspacing="0" style="background:#ffffff; margin:40px auto; border-radius:8px; box-shadow: 0 4px 8px rgba(0,0,0,0.1);">
      <tr>
        <td style="padding:30px; text-align:center; border-bottom:1px solid #eee;">
          <h2 style="color:#333333; margin:0;">验证码验证</h2>
          <p style="color:#666666; font-size:14px; margin-top:8px;">请使用下面的验证码完成验证</p>
        </td>
      </tr>
      <tr>
        <td style="padding:40px 0; text-align:center;">
          <div style="display:inline-block; font-size:28px; letter-spacing:6px; font-weight:bold; color:#2d8cf0; padding:10px 20px; background:#f2f6fc; border-radius:6px; border:1px solid #d3e0f3;">
            %s
          </div>
        </td>
      </tr>
      <tr>
        <td style="padding:0 30px 40px; text-align:center; font-size:14px; color:#555555; line-height:1.5;">
          如果您没有请求此验证码，请忽略本邮件。<br/>
          感谢您的支持！
        </td>
      </tr>
      <tr>
        <td style="background:#f0f0f0; padding:20px 30px; text-align:center; font-size:12px; color:#999999;">
          &copy; 2025 SReader。保留所有权利。
        </td>
      </tr>
    </table>
  </body>
</html>`, captchaCode)

	err := sendEmail(EmailDialer.Username, targetUser, "Email地址验证", "text/html", htmlBody)

	return err
}
