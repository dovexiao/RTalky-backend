package utils

import (
	"encoding/base64"
	"errors"
	"github.com/sirupsen/logrus"
	"strconv"
	"time"

	"github.com/golang-jwt/jwt/v5"
)

type CustomClaims struct {
	Username string `json:"username"`
	jwt.RegisteredClaims
}

type JWTUtils struct {
	secretKey     []byte
	expireSeconds int64
}

func NewJWTUtils(secretInBase64 string, jwtExpireTimeMSStr string) *JWTUtils {
	secret := base64.StdEncoding.EncodeToString([]byte(secretInBase64))
	jwtExpireTimeMS, err := strconv.ParseInt(jwtExpireTimeMSStr, 10, 64)

	if err != nil {
		logrus.Fatalf("`%s` 不能转为合法的过期时间，请检查配置文件", jwtExpireTimeMSStr)
	}

	return &JWTUtils{
		secretKey:     []byte(secret),
		expireSeconds: jwtExpireTimeMS,
	}
}

func (j *JWTUtils) GenerateToken(username string) (string, error) {
	claims := CustomClaims{
		Username: username,
		RegisteredClaims: jwt.RegisteredClaims{
			ExpiresAt: jwt.NewNumericDate(time.Now().Add(time.Duration(j.expireSeconds) * time.Second)),
			IssuedAt:  jwt.NewNumericDate(time.Now()),
		},
	}

	token := jwt.NewWithClaims(jwt.SigningMethodHS256, claims)
	return token.SignedString(j.secretKey)
}

func (j *JWTUtils) ParseToken(tokenString string) (*CustomClaims, error) {
	token, err := jwt.ParseWithClaims(tokenString, &CustomClaims{}, func(token *jwt.Token) (interface{}, error) {
		return j.secretKey, nil
	})

	if err != nil {
		return nil, err
	}

	if claims, ok := token.Claims.(*CustomClaims); ok && token.Valid {
		return claims, nil
	}

	return nil, errors.New("invalid token")
}
