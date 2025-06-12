package dto

type Event struct {
	Id      string `json:"id"`
	Comment string `json:"comment"`
	Event   string `json:"event"`
	Data    string `json:"data"`
}

type Task struct {
	From string `json:"from"`
	To   string `json:"to"`
	Data Event  `json:"data"`
}

type SendArg struct {
	Target string `json:"target"`
	Data   Event  `json:"data"`
}
