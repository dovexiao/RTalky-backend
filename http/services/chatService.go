package services

import "RTalky/http/dto"

var TaskChan chan dto.Task

func AddTask(task dto.Task) {
	TaskChan <- task
}

func GetTaskChan(username string) (chan dto.Task, bool) {
	return TaskChan, true
}
