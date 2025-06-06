package services

import (
	"os"

	"RTalky/core/tools"
	"RTalky/database/ent"
)

var JwtUtils *tools.JWTUtils
var DatabaseClient *ent.Client

func Register(client *ent.Client) {
	DatabaseClient = client
	JwtUtils = tools.NewJWTUtils(os.Getenv("JWT_SECRET"), os.Getenv("JWT_EXPIRATION_TIME_MS"))
}
