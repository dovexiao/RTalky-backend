package logger

import (
	"io"
	"os"
	"path/filepath"

	"github.com/sirupsen/logrus"
	"gopkg.in/natefinch/lumberjack.v2"
)

func SetupLogger(logDirPath string, logLevel logrus.Level) {
	logFilePath := filepath.Join(logDirPath, "app-latest.log")

	fileLogger := &lumberjack.Logger{
		Filename:   logFilePath,
		MaxSize:    10,
		MaxBackups: 3,
		MaxAge:     28,
		Compress:   true,
	}

	// 合并多个输出
	multiWriter := io.MultiWriter(os.Stdout, fileLogger)

	logrus.SetOutput(multiWriter)
	// 设置为使用JSON格式输出
	// logrus.SetFormatter(&logrus.JSONFormatter{})
	logrus.SetFormatter(&logrus.TextFormatter{})
	logrus.SetLevel(logLevel)

	logrus.Debug("Setup logrus logger with logDirPath: ", logDirPath)
}
