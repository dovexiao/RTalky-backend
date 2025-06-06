package services

import (
	"RTalky/ent"
	"RTalky/utils"
	"os"
)

var JwtUtils *utils.JWTUtils
var DatabaseClient *ent.Client

func Register(client *ent.Client) {
	DatabaseClient = client
	JwtUtils = utils.NewJWTUtils(os.Getenv("JWT_SECRET"), os.Getenv("JWT_EXPIRATION_TIME_MS"))
}
