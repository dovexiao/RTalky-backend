package handlers

import (
	_ "RTalky/core/tools"
	_ "RTalky/http/dto"

	"RTalky/http/handlers/responses"
	myservices "RTalky/http/services"
	"github.com/labstack/echo/v4"
	"github.com/sirupsen/logrus"
	"net/http"
)

// GenerateCaptcha godoc
// @Summary      Generate a captcha
// @Description  Generate a captcha image
// @Tags         auth
// @Accept       json
// @Produce      json
// @Param        type query string true "request arg"
// @Success      200  {object}  tools.ResponseI[dto.CaptchaI[any]]
// @Failure      500  {object}  tools.ErrorResponse
// @Router       /auth/captcha [GET]
func GenerateCaptcha(c echo.Context) error {
	captchaType := c.QueryParam("type")

	captcha, err := myservices.MakeImageCaptcha(captchaType)

	if err != nil {
		responses.SetReturnValue(c, http.StatusInternalServerError, responses.InternalErrorResponse)
		return nil
	}

	logrus.Debug("Generate captcha: ", captcha)

	responses.SetReturnValue(c, http.StatusOK, captcha)
	return nil
}
