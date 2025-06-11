package services

import (
	"os"
	"strconv"

	"github.com/sirupsen/logrus"
	"gopkg.in/gomail.v2"

	"RTalky/core"
	"RTalky/core/tools"
	"RTalky/database/ent"
)

var JwtUtils *tools.JWTUtils
var DatabaseClient *ent.Client

func Register(client *ent.Client) {
	DatabaseClient = client
	CaptchaExpiringMap = core.NewExpiringMap[string, string]()
	JwtUtils = tools.NewJWTUtils(os.Getenv("JWT_SECRET"), os.Getenv("JWT_EXPIRATION_TIME_MS"))

	var smtp_port int
	port, err := strconv.ParseInt(os.Getenv("SMTP_PORT"), 10, 31)

	if err != nil {
		logrus.Fatal("Fail to init services mod, for SMTP_PORT is invalid, reason: ", err)
		panic(err)
	}

	smtp_port = int(port)

	smtpPort := smtp_port
	smtpPass := os.Getenv("SMTP_PASS")
	smtpHost := os.Getenv("SMTP_HOST")
	smtpUser := os.Getenv("SMTP_USER")

	EmailDialer = gomail.NewDialer(smtpHost, smtpPort, smtpUser, smtpPass)
}
