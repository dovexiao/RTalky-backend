package tools

import (
	"RTalky/database/ent"

	"golang.org/x/crypto/bcrypt"
)

func VerifyPassword(u *ent.User, plainPassword string) bool {
	err := bcrypt.CompareHashAndPassword([]byte(*u.Password), []byte(plainPassword))
	return err == nil
}
