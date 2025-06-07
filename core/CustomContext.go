package core

import (
	"net/http"

	"RTalky/core/tools"
	"RTalky/handlers/responses"

	"github.com/labstack/echo/v4"
	"github.com/sirupsen/logrus"
)

type CustomContext struct {
	echo.Context
}

func (c *CustomContext) JSON(code int, i interface{}) error {
	var ok bool
	var resp tools.Response

	// 如果已经是 Response 类型，直接返回，否则包装
	if resp, ok = i.(tools.Response); !ok {
		resp = tools.Response{
			Code:    0,
			Message: "success",
			Data:    i,
		}
	}

	// 检查code和data的合法性
	if (resp.Code == 0 && resp.Data == nil) || (resp.Code != 0 && resp.Data != nil) {
		logrus.Errorf("Response returned a success code (%d), but no data was provided (data is %v), which is inconsistent with the expected behavior.\nresponse: %v\n", resp.Code, resp.Data, resp)
		responses.SetReturnValue(c.Context, http.StatusInternalServerError, responses.InternalErrorResponse)
		return nil
	}

	responses.SetReturnValue(c.Context, code, resp)
	return nil
}
