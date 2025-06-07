package core

import (
	"RTalky/core/tools"
	"github.com/labstack/echo/v4"
)

type CustomContext struct {
	echo.Context
}

func (c *CustomContext) JSON(code int, i interface{}) error {
	// 如果已经是 Response 类型，直接返回
	if _, ok := i.(tools.Response); ok {
		return c.Context.JSON(code, i)
	}

	// 否则包装
	resp := tools.Response{
		Code:    0,
		Message: "success",
		Data:    i,
	}
	return c.Context.JSON(code, resp)
}
