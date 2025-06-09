package tools

// Response represents a standard API response structure.
//
// It was originally used to encapsulate a response code, message, and optional data
// returned by API endpoints. The Data field used an empty interface{}, which made
// it flexible but lacked compile-time type safety.
//
// Deprecated: use ResponseI[T] instead. ResponseI provides improved flexibility, better
// type safety through generics, and clearer intent when handling API responses.
//
// Example migration:
//
//	var old Response
//	var new ResponseI[DataType]
type Response struct {
	Code    int         `json:"code"`
	Message string      `json:"message"`
	Data    interface{} `json:"data,omitempty"`
}

// ResponseI represents a generic API response structure.
//
// It encapsulates a response code, message, and typed data returned by API endpoints.
// By using a generic parameter T, ResponseI provides compile-time type safety and
// eliminates the need for type assertions when accessing the Data field.
//
// Typical use case:
//
//	ResponseI[User] — when returning a User object
//	ResponseI[[]Item] — when returning a list of Item objects
//	ResponseI[struct{}] — when no data is needed (empty payload)
//
// Example:
//
//	resp := ResponseI[User]{ Code: 0, Message: "success", Data: use
//	resp := ResponseI[User]{ Code: 0, Message: "success", Data: user }
//
// See also: deprecated Response struct for legacy usage.
type ResponseI[T any] struct {
	Code    int    `json:"code"`
	Message string `json:"message"`
	Data    T      `json:"data,omitempty"`
}

// ErrorResponse represents a standard error response returned by the API.
//
// It is typically used to convey error information when an API request fails.
// The client can inspect the `Code` and `Message` fields to understand the cause of the error.
type ErrorResponse struct {
	Code    int    `json:"code"`
	Message string `json:"message"`
}
