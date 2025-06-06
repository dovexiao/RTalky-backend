package core

import (
	"RTalky/utils"
	"github.com/labstack/echo/v4"
)

type CustomContext struct {
	echo.Context
}

func (c *CustomContext) JSON(code int, i interface{}) error {
	// 如果已经是 Response 类型，直接返回
	if _, ok := i.(utils.Response); ok {
		return c.Context.JSON(code, i)
	}

	// 否则包装
	resp := utils.Response{
		Code:    0,
		Message: "success",
		Data:    i,
	}
	return c.Context.JSON(code, resp)
}
