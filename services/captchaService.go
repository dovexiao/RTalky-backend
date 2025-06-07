package services

import (
	"bytes"
	"encoding/base64"
	"fmt"
	"github.com/dchest/captcha"
	"github.com/google/uuid"
	"image/png"
	"strings"
	"time"
)

type Captcha struct {
	Id      string `json:"id"`
	Captcha string `json:"captcha"`
}

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

func MakeCaptcha() (*Captcha, error) {
	answer, captchaURI, err := generateCaptcha()
	if err != nil {
		return nil, err
	}

	id := uuid.New().String()

	CaptchaExpiringMap.Set(id, answer, 5*time.Minute)

	return &Captcha{
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

	return true, "success"
}
