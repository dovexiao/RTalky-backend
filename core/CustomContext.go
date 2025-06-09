package core

import (
	"RTalky/http/handlers/responses"
	"net/http"

	"RTalky/core/tools"
	"github.com/labstack/echo/v4"
	"github.com/sirupsen/logrus"
)

type CustomContext struct {
	echo.Context
}

func (c *CustomContext) JSON(code int, i interface{}) error {
	var ok bool
	var resp tools.ResponseI[any]

	// 如果已经是 ResponseI 类型，直接返回
	if resp, ok = i.(tools.ResponseI[any]); !ok {
		// 如果是 ErrorResponse 类型，则将其赋值为ResponseI类型
		if errResp, ok := i.(tools.ErrorResponse); ok {
			resp = tools.ResponseI[any]{
				Code:    errResp.Code,
				Message: errResp.Message,
				Data:    nil,
			}
		} else
		// 否则包装
		{
			resp = tools.ResponseI[any]{
				Code:    0,
				Message: "success",
				Data:    i,
			}
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
