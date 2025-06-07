package services

import (
	"RTalky/core"
	"os"

	"RTalky/core/tools"
	"RTalky/database/ent"
)

var JwtUtils *tools.JWTUtils
var DatabaseClient *ent.Client
var CaptchaExpiringMap *core.ExpiringMap[string, string]

func Register(client *ent.Client) {
	DatabaseClient = client
	CaptchaExpiringMap = core.NewExpiringMap[string, string]()
	JwtUtils = tools.NewJWTUtils(os.Getenv("JWT_SECRET"), os.Getenv("JWT_EXPIRATION_TIME_MS"))
}
