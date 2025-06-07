package handlers

import (
	"RTalky/handlers/responses"
	"RTalky/services"
	"net/http"

	"RTalky/core/tools"
	"RTalky/database/dto"
	"RTalky/database/ent"
	"RTalky/database/ent/user"
	"github.com/labstack/echo/v4"
	"github.com/sirupsen/logrus"
)

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
		responses.SetReturnValue(c, http.StatusUnauthorized, responses.UnauthorizedResponse)
		return nil
	default:
		logrus.Errorln("Fail to query user: ", err)
		responses.SetReturnValue(c, http.StatusInternalServerError, responses.InternalErrorResponse)
	}
	return nil
}

func Logout(c echo.Context) error {
	responses.SetReturnValue(c, http.StatusOK, nil)
	return nil
}
