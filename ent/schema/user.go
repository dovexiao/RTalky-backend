package schema

import (
	"RTalky/ent/hook"
	"RTalky/utils"
	"golang.org/x/crypto/bcrypt"
	"golang.org/x/net/context"

	"entgo.io/ent"
	"entgo.io/ent/dialect/entsql"
	"entgo.io/ent/schema/field"
)

// User holds the schema definition for the User entity.
type User struct {
	ent.Schema
}

// Fields of the User.
func (User) Fields() []ent.Field {
	return []ent.Field{
		// 用户ID
		field.Int("id").
			Unique().
			Positive().
			Immutable().
			Annotations(entsql.Annotation{
				Incremental: utils.Ptr(true),
			}),

		// 用户名
		field.String("username").
			Unique(),

		// 昵称
		field.String("nickname"),

		// 简介
		field.String("introduction"),

		// 密码
		field.String("password").
			NotEmpty().
			Sensitive(),
	}
}

// Edges of the User.
func (User) Edges() []ent.Edge {
	return nil
}

// Hooks of the User.
func (User) Hooks() []ent.Hook {
	return []ent.Hook{
		hook.On(passwordHashHook, ent.OpCreate|ent.OpUpdate),
	}
}

func passwordHashHook(next ent.Mutator) ent.Mutator {
	return ent.MutateFunc(func(ctx context.Context, m ent.Mutation) (ent.Value, error) {
		if password, ok := m.Field("password"); ok {
			hashed, err := bcrypt.GenerateFromPassword([]byte(password.(string)), bcrypt.DefaultCost)
			if err != nil {
				return nil, err
			}
			_ = m.SetField("password", string(hashed))
		}
		return next.Mutate(ctx, m)
	})
}
