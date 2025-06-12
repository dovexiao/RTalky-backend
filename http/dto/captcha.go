package dto

import (
	"RTalky/core/tools"
	"strings"
)

const SlideCaptchaMaxOffset = 10

type AnswerChaker interface {
	Check(input any) bool
}

type CaptchaI[T any] struct {
	Id      string `json:"id"`
	Captcha T      `json:"captcha"`
}

type CaptchaInfoI[Tx any, Ty any] struct {
	Challenge Tx `json:"challenge"`
	Solution  Ty `json:"answer"`
}

type AnswerI[T any] struct {
	Type   string `json:"type"`
	Answer T      `json:"answer"`
}

type Size struct {
	Height int `json:"height"`
	Weight int `json:"weight"`
}

type SlideCaptchaChallenge struct {
	MasterImage string `json:"masterImage"`
	TileImage   string `json:"tileImage"`

	MasterSize Size `json:"masterSize"`
	TileSize   Size `json:"tileSize"`

	XAxis int `json:"XAxis"`
	YAxis int `json:"YAxis"`
}
type SlideCaptchaAnswer AnswerI[int]

type DigitCaptchaChallenge string
type DigitCaptchaAnswer AnswerI[string]

type SlideCaptchaInfo CaptchaInfoI[SlideCaptchaChallenge, SlideCaptchaAnswer]
type DigitCaptchaInfo CaptchaInfoI[DigitCaptchaChallenge, DigitCaptchaAnswer]

func (receiver SlideCaptchaAnswer) Check(input any) bool {
	if inputVal, ok := input.(int); ok {
		if tools.Abs(receiver.Answer-inputVal) <= SlideCaptchaMaxOffset {
			return true
		}
	}
	return false
}

func (receiver DigitCaptchaAnswer) Check(input any) bool {
	if inputVal, ok := input.(string); ok {
		if strings.EqualFold(receiver.Answer, inputVal) {
			return true
		}
	}
	return false
}
