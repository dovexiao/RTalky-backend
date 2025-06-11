package handlers

import (
	"RTalky/http/dto"
	"RTalky/http/handlers/responses"
	myservices "RTalky/http/services"
	"net/http"

	"RTalky/core/tools"
	"RTalky/database/ent"
	"RTalky/database/ent/user"
	"github.com/labstack/echo/v4"
	"github.com/sirupsen/logrus"
)

const (
	StatusOK           int = 0
	StatusCaptchaError int = iota + 1000
	StatusLoginError
	StatusSignupError
)

// Me godoc
// @Summary      Get current login account info
// @Description  get info by HTTP Authorization header
// @Tags         auth
// @Accept       json
// @Produce      json
// @Param        Authorization header string false "Authorization header"
// @Success      200  {object}  tools.ResponseI[dto.User]
// @Failure      401  {object}  tools.ErrorResponse
// @Failure      500  {object}  tools.ErrorResponse
// @Router       /auth/me [GET]
func Me(c echo.Context) error {
	username, ok := c.Get("username").(string)

	if !ok {
		responses.SetReturnValue(c, http.StatusUnauthorized, responses.UnauthorizedResponse)
		return nil
	}

	ctx := c.Request().Context()

	userToFind, err := myservices.DatabaseClient.User.
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
// @Param        arg body dto.LoginArg true "request arg"
// @Success      200  {object}  tools.ResponseI[dto.LoginResponse]
// @Success      200  {object}  tools.ErrorResponse
// @Failure      400  {object}  tools.ErrorResponse
// @Failure      500  {object}  tools.ErrorResponse
// @Router       /auth/login [POST]
func Login(c echo.Context) error {
	var loginDTO dto.LoginArg

	ctx := c.Request().Context()
	if err := c.Bind(&loginDTO); err != nil {
		logrus.Errorln("Fail to bind value to dto type: ", err)
		responses.SetReturnValue(c, http.StatusBadRequest, responses.ParametersErrorResponse)
		return nil
	}

	if correct, reason := myservices.VerifyCaptcha(loginDTO.Captcha.Id, loginDTO.Captcha.Captcha); !correct {
		logrus.Debug("Fail to verify captcha, reason: ", reason)
		responses.SetReturnValue(c, http.StatusOK, tools.ResponseI[string]{
			Code:    StatusCaptchaError,
			Message: "Incorrect captcha code",
		})
		return nil
	}

	userToFind, err := myservices.DatabaseClient.User.
		Query().
		Where(user.UsernameEQ(loginDTO.Username)).
		Only(ctx)

	switch {
	case err == nil:
		correct := tools.VerifyPassword(userToFind, loginDTO.Password)
		if !correct {
			responses.SetReturnValue(c, http.StatusOK, tools.ErrorResponse{
				Code:    StatusLoginError,
				Message: "Invalid account or password.",
			})
			return nil
		}

		var token string
		token, err = myservices.JwtUtils.GenerateToken(userToFind.Username)

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
		responses.SetReturnValue(c, http.StatusOK, tools.ErrorResponse{
			Code:    StatusLoginError,
			Message: "Invalid account or password.",
		})
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
// @Success      200  {object}  tools.ResponseI[string]
// @Failure      500  {object}  tools.ErrorResponse
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
// @Failure      500  {object}  tools.ErrorResponse
// @Router       /auth/captcha [GET]
func GenerateCaptcha(c echo.Context) error {
	captcha, err := myservices.MakeCaptcha()

	if err != nil {
		responses.SetReturnValue(c, http.StatusInternalServerError, responses.InternalErrorResponse)
		return nil
	}

	responses.SetReturnValue(c, http.StatusOK, captcha)
	return nil
}

// EmailCaptchaHandler godoc
// @Summary      Generate an email captcha
// @Description  Generate an email captcha image
// @Tags         auth
// @Accept       json
// @Produce      json
// @Param        arg query dto.EmailCaptchaArg true "request arg"
// @Success      200  {object}  tools.ResponseI[dto.EmailCaptchaResponse]
// @Failure      500  {object}  tools.ErrorResponse
// @Router       /auth/captcha/email [GET]
func EmailCaptchaHandler(c echo.Context) error {
	var emailCaptchaArg dto.EmailCaptchaArg

	if err := c.Bind(&emailCaptchaArg); err != nil {
		logrus.Errorln("Fail to bind value to dto type: ", err)
		responses.SetReturnValue(c, http.StatusBadRequest, responses.ParametersErrorResponse)
		return nil
	}

	captcha, err := myservices.MakeCaptcha()

	if err != nil {
		responses.SetReturnValue(c, http.StatusInternalServerError, responses.InternalErrorResponse)
		return nil
	}

	err = myservices.SendImageCaptchaEmail(captcha.Captcha, emailCaptchaArg.Email)
	if err != nil {
		responses.SetReturnValue(c, http.StatusInternalServerError, responses.InternalErrorResponse)
		return nil
	}

	responses.SetReturnValue(c, http.StatusOK, dto.EmailCaptchaResponse{Id: captcha.Id})
	return nil
}

// SignUpHandler godoc
// @Summary      handle signup response
// @Description  Handle the signup response
// @Tags         auth
// @Accept       json
// @Produce      json
// @Param        arg body dto.SignUpArg true "request arg"
// @Success      200  {object}  tools.ResponseI[string]
// @Failure      200  {object}  tools.ErrorResponse
// @Failure      500  {object}  tools.ErrorResponse
// @Router       /auth/signup [POST]
func SignUpHandler(c echo.Context) error {
	var signupArg dto.SignUpArg

	ctx := c.Request().Context()
	if err := c.Bind(&signupArg); err != nil {
		logrus.Errorln("Fail to bind value to dto type: ", err)
		responses.SetReturnValue(c, http.StatusBadRequest, responses.ParametersErrorResponse)
		return nil
	}

	if correct, reason := myservices.VerifyCaptcha(signupArg.Captcha.Id, signupArg.Captcha.Captcha); !correct {
		logrus.Debug("Fail to verify captcha, reason: ", reason)
		responses.SetReturnValue(c, http.StatusOK, tools.ResponseI[string]{
			Code:    StatusCaptchaError,
			Message: "Incorrect captcha code",
		})
		return nil
	}

	_, err := myservices.DatabaseClient.User.
		Query().
		Where(user.UsernameEQ(signupArg.Email)).
		Only(ctx)

	switch {
	case err == nil:
		responses.SetReturnValue(c, http.StatusOK, tools.ErrorResponse{
			Code:    StatusSignupError,
			Message: "Account already exists",
		})
		return nil
	case ent.IsNotFound(err):
		userToSave := myservices.DatabaseClient.User.Create().
			SetUsername(signupArg.Email).
			SetNickname(signupArg.Email).
			SetPassword(signupArg.Password)

		_, err := userToSave.Save(ctx)

		if err != nil {
			logrus.Warnf("Fail to save user: %v, reason: %v", userToSave, err)
			responses.SetReturnValue(c, http.StatusInternalServerError, responses.InternalErrorResponse)
			return nil
		}

		responses.SetReturnValue(c, http.StatusOK, "Sign up successfully")
		return nil
	default:
		logrus.Errorln("Fail to query user: ", err)
		responses.SetReturnValue(c, http.StatusInternalServerError, responses.InternalErrorResponse)
	}

	return nil
}
