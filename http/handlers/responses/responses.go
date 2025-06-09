package responses

import (
	"net/http"

	"RTalky/core/tools"

	"github.com/labstack/echo/v4"
	"github.com/sirupsen/logrus"
)

var InternalErrorResponse = tools.ErrorResponse{
	Code:    -1,
	Message: "Server error occurred. Please try again later.",
}

var UnauthorizedResponse = tools.ErrorResponse{
	Code:    -2,
	Message: "Authentication required. Please log in.",
}

var ParametersErrorResponse = tools.ErrorResponse{
	Code:    -3,
	Message: "Invalid request parameters.",
}

func SetReturnValue(c echo.Context, returnStatus int, returnValue interface{}) {
	err := c.JSON(returnStatus, returnValue)
	if err == nil {
		return
	}

	logrus.Errorf("Fail to set return value: \n%v\n%v\n", returnValue, err)
	err = c.JSON(http.StatusInternalServerError, InternalErrorResponse)
	if err != nil {
		logrus.Fatalf("Fail to set return value with default error response: %v\n", err)
		panic(err)
	}
	return
}
