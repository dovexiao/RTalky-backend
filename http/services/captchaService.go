package services

import (
	"RTalky/http/dto"
	"bytes"
	"encoding/base64"
	"fmt"
	"image/png"
	"strings"
	"time"

	"RTalky/core"
	"github.com/dchest/captcha"
	"github.com/google/uuid"
)

var CaptchaExpiringMap *core.ExpiringMap[string, string]

func generateCaptcha() (string, string, error) {
	digits := captcha.RandomDigits(6)
	img := captcha.NewImage("", digits, 240, 80)

	var buf bytes.Buffer
	err := png.Encode(&buf, img)
	if err != nil {
		return "", "", err
	}

	var sb strings.Builder
	for _, d := range digits {
		sb.WriteByte('0' + d)
	}
	answer := sb.String()

	base64Img := base64.StdEncoding.EncodeToString(buf.Bytes())
	dataURI := fmt.Sprintf("data:image/png;base64,%s", base64Img)

	return answer, dataURI, nil
}

func MakeCaptcha() (*dto.Captcha, error) {
	answer, captchaURI, err := generateCaptcha()
	if err != nil {
		return nil, err
	}

	id := uuid.New().String()

	CaptchaExpiringMap.Set(id, answer, 5*time.Minute)

	return &dto.Captcha{
		Id:      id,
		Captcha: captchaURI,
	}, nil
}

func VerifyCaptcha(id, input string) (bool, string) {
	answer, ok := CaptchaExpiringMap.Get(id)

	if !ok {
		return false, "Captcha has expired"
	}

	if strings.EqualFold(answer, input) {
		return false, "Captcha verification failed"
	}

	CaptchaExpiringMap.Delete(id)

	return true, "success"
}
