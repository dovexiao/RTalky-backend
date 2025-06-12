package services

import (
	"RTalky/http/dto"
	"bytes"
	"encoding/base64"
	"errors"
	"fmt"
	"github.com/sirupsen/logrus"
	"image/png"
	"strings"
	"time"

	"RTalky/core"
	"github.com/dchest/captcha"
	"github.com/google/uuid"

	"github.com/wenlng/go-captcha-assets/resources/tiles"
	"github.com/wenlng/go-captcha/v2/slide"
)

var slideTileCapt slide.Captcha
var CaptchaExpiringMap *core.ExpiringMap[string, dto.AnswerChaker]

func ConvertTilesToSlide(graphs []*tiles.GraphImage) []*slide.GraphImage {
	result := make([]*slide.GraphImage, len(graphs))
	for i, g := range graphs {
		if g == nil {
			result[i] = nil
			continue
		}
		result[i] = &slide.GraphImage{
			OverlayImage: g.OverlayImage,
			ShadowImage:  g.ShadowImage,
			MaskImage:    g.MaskImage,
		}
	}
	return result
}

func GetAnswerFromDigits(digits []byte) string {
	var sb strings.Builder
	for _, d := range digits {
		sb.WriteByte('0' + d)
	}
	return sb.String()
}

func generateSlideCaptcha() (*dto.SlideCaptchaInfo, error) {
	captData, err := slideTileCapt.Generate()
	if err != nil {
		logrus.Error("Fail to generate captcha: ", err)
		return nil, err
	}

	blockData := captData.GetData()
	if blockData == nil {
		logrus.Error("Fail to generate captcha: ", err)
		return nil, err
	}

	var mBase64, tBase64 string

	masterImage := captData.GetMasterImage()
	mBase64, err = masterImage.ToBase64()
	if err != nil {
		logrus.Error("Fail to generate captcha: ", err)
		return nil, err
	}
	tileImage := captData.GetTileImage()
	tBase64, err = tileImage.ToBase64()
	if err != nil {
		logrus.Error("Fail to generate captcha: ", err)
		return nil, err
	}

	masterSize := masterImage.Get().Bounds().Max
	tileSize := tileImage.Get().Bounds().Max

	return &dto.SlideCaptchaInfo{
		Challenge: dto.SlideCaptchaChallenge{
			MasterImage: mBase64,
			TileImage:   tBase64,

			MasterSize: dto.Size{
				Height: masterSize.Y,
				Weight: masterSize.X,
			},
			TileSize: dto.Size{
				Height: tileSize.Y,
				Weight: tileSize.X,
			},

			XAxis: blockData.DX,
			YAxis: blockData.DY,
		},
		Solution: dto.SlideCaptchaAnswer{
			Type:   "slide",
			Answer: blockData.X,
		},
	}, nil
}

func generateDigitsCaptcha() (*dto.DigitCaptchaInfo, error) {
	digits := captcha.RandomDigits(6)
	img := captcha.NewImage("", digits, 240, 80)

	var buf bytes.Buffer
	err := png.Encode(&buf, img)
	if err != nil {
		return nil, err
	}

	answer := GetAnswerFromDigits(digits)

	base64Img := base64.StdEncoding.EncodeToString(buf.Bytes())
	dataURI := fmt.Sprintf("data:image/png;base64,%s", base64Img)

	return &dto.DigitCaptchaInfo{
		Challenge: dto.DigitCaptchaChallenge(dataURI),
		Solution: dto.DigitCaptchaAnswer{
			Type:   "digit",
			Answer: answer,
		},
	}, nil
}

func MakeImageCaptcha(captchaType string) (*dto.CaptchaI[any], error) {
	switch captchaType {
	case "digit":
		digitCaptcha, err := generateDigitsCaptcha()
		if err != nil {
			return nil, err
		}

		id := uuid.New().String()
		CaptchaExpiringMap.Set(id, digitCaptcha.Solution, 5*time.Minute)

		logrus.Debug("new captcha:", digitCaptcha.Solution)

		return &dto.CaptchaI[any]{
			Id:      id,
			Captcha: digitCaptcha.Challenge,
		}, nil
	case "slide":
		slideCaptcha, err := generateSlideCaptcha()
		if err != nil {
			return nil, err
		}

		id := uuid.New().String()
		CaptchaExpiringMap.Set(id, slideCaptcha.Solution, 5*time.Minute)

		logrus.Debug("new captcha:", slideCaptcha.Solution)

		return &dto.CaptchaI[any]{
			Id:      id,
			Captcha: slideCaptcha.Challenge,
		}, nil
	}

	errorMessage := fmt.Sprintf("captcha type `%s` is not implemented", captchaType)
	return nil, errors.New(errorMessage)
}

func VerifyCaptcha(id string, input any) (bool, string) {
	answer, ok := CaptchaExpiringMap.Get(id)

	if !ok {
		return false, "Captcha has expired"
	}

	CaptchaExpiringMap.Delete(id)

	if !answer.Check(input) {
		logrus.Debugf("Verify captcha fail: answer=`%s` input=`%s`", answer, input)
		return false, "Captcha verification failed"
	}

	return true, "success"
}
