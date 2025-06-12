package handlers

import (
	"RTalky/core/event"
	"RTalky/http/dto"
	"RTalky/http/handlers/responses"
	"RTalky/http/services"
	"github.com/labstack/echo/v4"
	"github.com/sirupsen/logrus"
	"net/http"
)

func SendMessageHandler(c echo.Context) error {
	var sendArg dto.SendArg

	if err := c.Bind(&sendArg); err != nil {
		logrus.Errorln("Fail to bind value to dto type: ", err)
		responses.SetReturnValue(c, http.StatusBadRequest, responses.ParametersErrorResponse)
		return nil
	}

	username, ok := c.Get("username").(string)

	if !ok {
		responses.SetReturnValue(c, http.StatusUnauthorized, responses.UnauthorizedResponse)
		return nil
	}

	services.AddTask(dto.Task{
		From: username,
		To:   sendArg.Target,
		Data: sendArg.Data,
	})
	responses.SetReturnValue(c, http.StatusOK, "Send successfully")
	return nil
}

func ServerEventHandler(c echo.Context) error {
	username, ok := c.Get("username").(string)

	if !ok {
		responses.SetReturnValue(c, http.StatusUnauthorized, responses.UnauthorizedResponse)
		return nil
	}

	logrus.Debugf("SSE client connected, ip: %v, name: %v", c.RealIP(), username)

	w := c.Response()
	w.Header().Set("Content-Type", "text/event-stream")
	w.Header().Set("Cache-Control", "no-cache")
	w.Header().Set("Connection", "keep-alive")

	taskChan, ok := services.GetTaskChan(username)

	if !ok {
		responses.SetReturnValue(c, http.StatusInternalServerError, responses.InternalErrorResponse)
		return nil
	}

	for {
		select {
		case <-c.Request().Context().Done():
			logrus.Debugf("SSE client disconnected, ip: %v", c.RealIP())
			return nil
		case task, ok := <-taskChan:
			if !ok {
				logrus.Debug("任务通道已关闭，停止发送")
				return nil
			}

			// 构造 SSE 消息
			eventInst := event.Event{
				ID:      []byte(task.Data.Id),
				Data:    []byte(task.Data.Data),
				Event:   []byte(task.Data.Event),
				Retry:   []byte("3000"),
				Comment: []byte(task.Data.Comment),
			}

			if _, err := eventInst.WriteTo(w); err != nil {
				return err
			}

			w.Flush()
		}
	}
}
