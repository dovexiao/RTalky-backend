package handlers

import (
	"net/http"

	"RTalky/core/tools"
	"RTalky/database/dto"
	"RTalky/database/ent"
	"RTalky/database/ent/user"
	"RTalky/handlers/responses"
	"RTalky/services"

	"github.com/labstack/echo/v4"
	"github.com/sirupsen/logrus"
)

// Me godoc
// @Summary      Get current login account info
// @Description  get info by HTTP Authorization header
// @Tags         auth
// @Accept       json
// @Produce      json
// @Param        Authorization header string false "Authorization header"
// @Success      200  {object}  tools.ResponseI[dto.User]
// @Failure      401  {object}  tools.ResponseI[any]
// @Failure      500  {object}  tools.ResponseI[any]
// @Router       /auth/me [GET]
func Me(c echo.Context) error {
	username, ok := c.Get("username").(string)

	if !ok {
		responses.SetReturnValue(c, http.StatusUnauthorized, responses.UnauthorizedResponse)
		return nil
	}

	ctx := c.Request().Context()

	userToFind, err := services.DatabaseClient.User.
		Query().
		Where(user.UsernameEQ(username)).
		Only(ctx)

	switch {
	case err == nil:
		responses.SetReturnValue(c, http.StatusOK, dto.NewUser(userToFind))
		return nil
	case ent.IsNotFound(err):
		responses.SetReturnValue(c, http.StatusUnauthorized, responses.UnauthorizedResponse)
		return nil
	default:
		logrus.Errorln("Fail to query user: ", err)
		responses.SetReturnValue(c, http.StatusInternalServerError, responses.InternalErrorResponse)
	}
	return nil
}

// Login godoc
// @Summary      login with username and password
// @Description  login with username and password
// @Tags         auth
// @Accept       json
// @Produce      json
// @Param        username body string true "username"
// @Param        password body string true "password"
// @Success      200  {object}  tools.ResponseI[dto.LoginResponse]
// @Failure      400  {object}  tools.ResponseI[any]
// @Failure      500  {object}  tools.ResponseI[any]
// @Router       /auth/login [POST]
func Login(c echo.Context) error {
	var loginDTO dto.LoginArg

	ctx := c.Request().Context()
	if err := c.Bind(&loginDTO); err != nil {
		logrus.Errorln("Fail to bind value to dto type: ", err)
		responses.SetReturnValue(c, http.StatusBadRequest, responses.ParametersErrorResponse)
		return nil
	}

	userToFind, err := services.DatabaseClient.User.
		Query().
		Where(user.UsernameEQ(loginDTO.Password)).
		Only(ctx)

	switch {
	case err == nil:
		correct := tools.VerifyPassword(userToFind, loginDTO.Password)
		if !correct {
			responses.SetReturnValue(c, http.StatusOK, responses.AccountOrPasswordErrorResponse)
			return nil
		}

		var token string
		token, err = services.JwtUtils.GenerateToken(userToFind.Username)

		if err != nil {
			logrus.Errorln("Fail to generate token: ", err)
			responses.SetReturnValue(c, http.StatusInternalServerError, responses.InternalErrorResponse)
			return nil
		}

		responses.SetReturnValue(c, http.StatusOK, dto.LoginResponse{
			Type:  "Bearer",
			Token: token,
		})
		return nil
	case ent.IsNotFound(err):
		responses.SetReturnValue(c, http.StatusOK, responses.AccountOrPasswordErrorResponse)
		return nil
	default:
		logrus.Errorln("Fail to query user: ", err)
		responses.SetReturnValue(c, http.StatusInternalServerError, responses.InternalErrorResponse)
	}
	return nil
}

// Logout godoc
// @Summary      logout
// @Description  logout
// @Tags         auth
// @Accept       json
// @Produce      json
// @Param        Authorization header string false "Authorization header"
// @Success      200  {object}  tools.ResponseI[any]
// @Router       /auth/logout [POST]
func Logout(c echo.Context) error {
	responses.SetReturnValue(c, http.StatusOK, "Logout successfully")
	return nil
}

// GenerateCaptcha godoc
// @Summary      Generate a captcha
// @Description  Generate a captcha image
// @Tags         auth
// @Accept       json
// @Produce      json
// @Success      200  {object}  tools.ResponseI[dto.Captcha]
// @Failure      500  {object}  tools.ResponseI[any]
// @Router       /auth/captcha [GET]
func GenerateCaptcha(c echo.Context) error {
	captcha, err := services.MakeCaptcha()

	if err != nil {
		responses.SetReturnValue(c, http.StatusInternalServerError, responses.InternalErrorResponse)
		return nil
	}

	responses.SetReturnValue(c, http.StatusOK, captcha)
	return nil
}
