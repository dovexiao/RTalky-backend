package responses

import (
	"net/http"

	"RTalky/core/tools"

	"github.com/labstack/echo/v4"
	"github.com/sirupsen/logrus"
)

var InternalErrorResponse = tools.Response{
	Code:    -1,
	Message: "Server error occurred. Please try again later.",
	Data:    nil,
}

var UnauthorizedResponse = tools.Response{
	Code:    -2,
	Message: "Authentication required. Please log in.",
	Data:    nil,
}

var ParametersErrorResponse = tools.Response{
	Code:    -3,
	Message: "Invalid request parameters.",
	Data:    nil,
}

var AccountOrPasswordErrorResponse = tools.Response{
	Code:    -4,
	Message: "Invalid account or password.",
	Data:    nil,
}

func SetReturnValue(c echo.Context, returnStatus int, returnValue interface{}) {
	err := c.JSON(returnStatus, returnValue)
	if err != nil {
		logrus.Errorf("Fail to set return value: \n%v\n%v\n", returnValue, err)
	}
	err = c.JSON(http.StatusInternalServerError, InternalErrorResponse)
	if err != nil {
		logrus.Fatalf("Fail to set return value with default error response: %v\n", err)
		panic(err)
	}
	return
}
