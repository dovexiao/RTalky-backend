package schema

import (
	"entgo.io/ent"
	"entgo.io/ent/schema/field"
)

// Oauth holds the schema definition for the Oauth entity.
type Oauth struct {
	ent.Schema
}

// Fields of the Oauth.
func (Oauth) Fields() []ent.Field {
	return []ent.Field{
		field.String("provider"),
	}
}

// Edges of the Oauth.
func (Oauth) Edges() []ent.Edge {
	return nil
}
