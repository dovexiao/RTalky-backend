package database

import (
	"context"

	"RTalky/database/ent"

	"github.com/sirupsen/logrus"
)

func GetDataBaseClient(dbDriver, dbURL string) (*ent.Client, error) {
	client, err := ent.Open(dbDriver, dbURL)
	if err != nil {
		logrus.Error("连接数据库失败: ", err.Error())
		return nil, err
	}

	// Run the auto migration tool.
	ctx := context.Background()
	if err := client.Schema.Create(ctx); err != nil {
		logrus.Error("更新数据库失败", err.Error())
		return nil, err
	}

	client.User.Create().SetPassword("123")

	//defer client.Close()
	return client, nil
}
