package dto

import (
	"time"

	"RTalky/database/ent"
)

type User struct {
	Username     string    `json:"username"`
	Avatar       string    `json:"avatar"`
	Nickname     string    `json:"nickname"`
	Introduction string    `json:"introduction"`
	CreateAt     time.Time `json:"create_at"`
}

type SignUpArg[T any] struct {
	Email    string      `json:"email"`
	Password string      `json:"password"`
	Captcha  CaptchaI[T] `json:"captcha"`
}

type LoginArg[T any] struct {
	Username string      `json:"username"`
	Password string      `json:"password"`
	Captcha  CaptchaI[T] `json:"captcha"`
}

type LoginResponse struct {
	Type  string `json:"type"`
	Token string `json:"token"`
}

func NewUser(user *ent.User) (u User) {
	u.Username = user.Username
	u.Avatar = *user.Avatar
	u.Nickname = user.Nickname
	u.Introduction = user.Introduction
	u.CreateAt = user.CreateAt
	return
}

type EmailCaptchaArg struct {
	Email string `query:"email"`
}

type EmailCaptchaResponse struct {
	Id string `json:"id"`
}
