// Code generated by ent, DO NOT EDIT.

package user

import (
	"RTalky/database/ent/predicate"
	"time"

	"entgo.io/ent/dialect/sql"
)

// ID filters vertices based on their ID field.
func ID(id int) predicate.User {
	return predicate.User(sql.FieldEQ(FieldID, id))
}

// IDEQ applies the EQ predicate on the ID field.
func IDEQ(id int) predicate.User {
	return predicate.User(sql.FieldEQ(FieldID, id))
}

// IDNEQ applies the NEQ predicate on the ID field.
func IDNEQ(id int) predicate.User {
	return predicate.User(sql.FieldNEQ(FieldID, id))
}

// IDIn applies the In predicate on the ID field.
func IDIn(ids ...int) predicate.User {
	return predicate.User(sql.FieldIn(FieldID, ids...))
}

// IDNotIn applies the NotIn predicate on the ID field.
func IDNotIn(ids ...int) predicate.User {
	return predicate.User(sql.FieldNotIn(FieldID, ids...))
}

// IDGT applies the GT predicate on the ID field.
func IDGT(id int) predicate.User {
	return predicate.User(sql.FieldGT(FieldID, id))
}

// IDGTE applies the GTE predicate on the ID field.
func IDGTE(id int) predicate.User {
	return predicate.User(sql.FieldGTE(FieldID, id))
}

// IDLT applies the LT predicate on the ID field.
func IDLT(id int) predicate.User {
	return predicate.User(sql.FieldLT(FieldID, id))
}

// IDLTE applies the LTE predicate on the ID field.
func IDLTE(id int) predicate.User {
	return predicate.User(sql.FieldLTE(FieldID, id))
}

// IsDeleted applies equality check predicate on the "is_deleted" field. It's identical to IsDeletedEQ.
func IsDeleted(v bool) predicate.User {
	return predicate.User(sql.FieldEQ(FieldIsDeleted, v))
}

// Username applies equality check predicate on the "username" field. It's identical to UsernameEQ.
func Username(v string) predicate.User {
	return predicate.User(sql.FieldEQ(FieldUsername, v))
}

// Nickname applies equality check predicate on the "nickname" field. It's identical to NicknameEQ.
func Nickname(v string) predicate.User {
	return predicate.User(sql.FieldEQ(FieldNickname, v))
}

// Introduction applies equality check predicate on the "introduction" field. It's identical to IntroductionEQ.
func Introduction(v string) predicate.User {
	return predicate.User(sql.FieldEQ(FieldIntroduction, v))
}

// Avatar applies equality check predicate on the "avatar" field. It's identical to AvatarEQ.
func Avatar(v string) predicate.User {
	return predicate.User(sql.FieldEQ(FieldAvatar, v))
}

// CreateAt applies equality check predicate on the "create_at" field. It's identical to CreateAtEQ.
func CreateAt(v time.Time) predicate.User {
	return predicate.User(sql.FieldEQ(FieldCreateAt, v))
}

// LastLogin applies equality check predicate on the "last_login" field. It's identical to LastLoginEQ.
func LastLogin(v time.Time) predicate.User {
	return predicate.User(sql.FieldEQ(FieldLastLogin, v))
}

// UpdatedAt applies equality check predicate on the "updated_at" field. It's identical to UpdatedAtEQ.
func UpdatedAt(v time.Time) predicate.User {
	return predicate.User(sql.FieldEQ(FieldUpdatedAt, v))
}

// Password applies equality check predicate on the "password" field. It's identical to PasswordEQ.
func Password(v string) predicate.User {
	return predicate.User(sql.FieldEQ(FieldPassword, v))
}

// IsDeletedEQ applies the EQ predicate on the "is_deleted" field.
func IsDeletedEQ(v bool) predicate.User {
	return predicate.User(sql.FieldEQ(FieldIsDeleted, v))
}

// IsDeletedNEQ applies the NEQ predicate on the "is_deleted" field.
func IsDeletedNEQ(v bool) predicate.User {
	return predicate.User(sql.FieldNEQ(FieldIsDeleted, v))
}

// UsernameEQ applies the EQ predicate on the "username" field.
func UsernameEQ(v string) predicate.User {
	return predicate.User(sql.FieldEQ(FieldUsername, v))
}

// UsernameNEQ applies the NEQ predicate on the "username" field.
func UsernameNEQ(v string) predicate.User {
	return predicate.User(sql.FieldNEQ(FieldUsername, v))
}

// UsernameIn applies the In predicate on the "username" field.
func UsernameIn(vs ...string) predicate.User {
	return predicate.User(sql.FieldIn(FieldUsername, vs...))
}

// UsernameNotIn applies the NotIn predicate on the "username" field.
func UsernameNotIn(vs ...string) predicate.User {
	return predicate.User(sql.FieldNotIn(FieldUsername, vs...))
}

// UsernameGT applies the GT predicate on the "username" field.
func UsernameGT(v string) predicate.User {
	return predicate.User(sql.FieldGT(FieldUsername, v))
}

// UsernameGTE applies the GTE predicate on the "username" field.
func UsernameGTE(v string) predicate.User {
	return predicate.User(sql.FieldGTE(FieldUsername, v))
}

// UsernameLT applies the LT predicate on the "username" field.
func UsernameLT(v string) predicate.User {
	return predicate.User(sql.FieldLT(FieldUsername, v))
}

// UsernameLTE applies the LTE predicate on the "username" field.
func UsernameLTE(v string) predicate.User {
	return predicate.User(sql.FieldLTE(FieldUsername, v))
}

// UsernameContains applies the Contains predicate on the "username" field.
func UsernameContains(v string) predicate.User {
	return predicate.User(sql.FieldContains(FieldUsername, v))
}

// UsernameHasPrefix applies the HasPrefix predicate on the "username" field.
func UsernameHasPrefix(v string) predicate.User {
	return predicate.User(sql.FieldHasPrefix(FieldUsername, v))
}

// UsernameHasSuffix applies the HasSuffix predicate on the "username" field.
func UsernameHasSuffix(v string) predicate.User {
	return predicate.User(sql.FieldHasSuffix(FieldUsername, v))
}

// UsernameIsNil applies the IsNil predicate on the "username" field.
func UsernameIsNil() predicate.User {
	return predicate.User(sql.FieldIsNull(FieldUsername))
}

// UsernameNotNil applies the NotNil predicate on the "username" field.
func UsernameNotNil() predicate.User {
	return predicate.User(sql.FieldNotNull(FieldUsername))
}

// UsernameEqualFold applies the EqualFold predicate on the "username" field.
func UsernameEqualFold(v string) predicate.User {
	return predicate.User(sql.FieldEqualFold(FieldUsername, v))
}

// UsernameContainsFold applies the ContainsFold predicate on the "username" field.
func UsernameContainsFold(v string) predicate.User {
	return predicate.User(sql.FieldContainsFold(FieldUsername, v))
}

// NicknameEQ applies the EQ predicate on the "nickname" field.
func NicknameEQ(v string) predicate.User {
	return predicate.User(sql.FieldEQ(FieldNickname, v))
}

// NicknameNEQ applies the NEQ predicate on the "nickname" field.
func NicknameNEQ(v string) predicate.User {
	return predicate.User(sql.FieldNEQ(FieldNickname, v))
}

// NicknameIn applies the In predicate on the "nickname" field.
func NicknameIn(vs ...string) predicate.User {
	return predicate.User(sql.FieldIn(FieldNickname, vs...))
}

// NicknameNotIn applies the NotIn predicate on the "nickname" field.
func NicknameNotIn(vs ...string) predicate.User {
	return predicate.User(sql.FieldNotIn(FieldNickname, vs...))
}

// NicknameGT applies the GT predicate on the "nickname" field.
func NicknameGT(v string) predicate.User {
	return predicate.User(sql.FieldGT(FieldNickname, v))
}

// NicknameGTE applies the GTE predicate on the "nickname" field.
func NicknameGTE(v string) predicate.User {
	return predicate.User(sql.FieldGTE(FieldNickname, v))
}

// NicknameLT applies the LT predicate on the "nickname" field.
func NicknameLT(v string) predicate.User {
	return predicate.User(sql.FieldLT(FieldNickname, v))
}

// NicknameLTE applies the LTE predicate on the "nickname" field.
func NicknameLTE(v string) predicate.User {
	return predicate.User(sql.FieldLTE(FieldNickname, v))
}

// NicknameContains applies the Contains predicate on the "nickname" field.
func NicknameContains(v string) predicate.User {
	return predicate.User(sql.FieldContains(FieldNickname, v))
}

// NicknameHasPrefix applies the HasPrefix predicate on the "nickname" field.
func NicknameHasPrefix(v string) predicate.User {
	return predicate.User(sql.FieldHasPrefix(FieldNickname, v))
}

// NicknameHasSuffix applies the HasSuffix predicate on the "nickname" field.
func NicknameHasSuffix(v string) predicate.User {
	return predicate.User(sql.FieldHasSuffix(FieldNickname, v))
}

// NicknameEqualFold applies the EqualFold predicate on the "nickname" field.
func NicknameEqualFold(v string) predicate.User {
	return predicate.User(sql.FieldEqualFold(FieldNickname, v))
}

// NicknameContainsFold applies the ContainsFold predicate on the "nickname" field.
func NicknameContainsFold(v string) predicate.User {
	return predicate.User(sql.FieldContainsFold(FieldNickname, v))
}

// IntroductionEQ applies the EQ predicate on the "introduction" field.
func IntroductionEQ(v string) predicate.User {
	return predicate.User(sql.FieldEQ(FieldIntroduction, v))
}

// IntroductionNEQ applies the NEQ predicate on the "introduction" field.
func IntroductionNEQ(v string) predicate.User {
	return predicate.User(sql.FieldNEQ(FieldIntroduction, v))
}

// IntroductionIn applies the In predicate on the "introduction" field.
func IntroductionIn(vs ...string) predicate.User {
	return predicate.User(sql.FieldIn(FieldIntroduction, vs...))
}

// IntroductionNotIn applies the NotIn predicate on the "introduction" field.
func IntroductionNotIn(vs ...string) predicate.User {
	return predicate.User(sql.FieldNotIn(FieldIntroduction, vs...))
}

// IntroductionGT applies the GT predicate on the "introduction" field.
func IntroductionGT(v string) predicate.User {
	return predicate.User(sql.FieldGT(FieldIntroduction, v))
}

// IntroductionGTE applies the GTE predicate on the "introduction" field.
func IntroductionGTE(v string) predicate.User {
	return predicate.User(sql.FieldGTE(FieldIntroduction, v))
}

// IntroductionLT applies the LT predicate on the "introduction" field.
func IntroductionLT(v string) predicate.User {
	return predicate.User(sql.FieldLT(FieldIntroduction, v))
}

// IntroductionLTE applies the LTE predicate on the "introduction" field.
func IntroductionLTE(v string) predicate.User {
	return predicate.User(sql.FieldLTE(FieldIntroduction, v))
}

// IntroductionContains applies the Contains predicate on the "introduction" field.
func IntroductionContains(v string) predicate.User {
	return predicate.User(sql.FieldContains(FieldIntroduction, v))
}

// IntroductionHasPrefix applies the HasPrefix predicate on the "introduction" field.
func IntroductionHasPrefix(v string) predicate.User {
	return predicate.User(sql.FieldHasPrefix(FieldIntroduction, v))
}

// IntroductionHasSuffix applies the HasSuffix predicate on the "introduction" field.
func IntroductionHasSuffix(v string) predicate.User {
	return predicate.User(sql.FieldHasSuffix(FieldIntroduction, v))
}

// IntroductionEqualFold applies the EqualFold predicate on the "introduction" field.
func IntroductionEqualFold(v string) predicate.User {
	return predicate.User(sql.FieldEqualFold(FieldIntroduction, v))
}

// IntroductionContainsFold applies the ContainsFold predicate on the "introduction" field.
func IntroductionContainsFold(v string) predicate.User {
	return predicate.User(sql.FieldContainsFold(FieldIntroduction, v))
}

// AvatarEQ applies the EQ predicate on the "avatar" field.
func AvatarEQ(v string) predicate.User {
	return predicate.User(sql.FieldEQ(FieldAvatar, v))
}

// AvatarNEQ applies the NEQ predicate on the "avatar" field.
func AvatarNEQ(v string) predicate.User {
	return predicate.User(sql.FieldNEQ(FieldAvatar, v))
}

// AvatarIn applies the In predicate on the "avatar" field.
func AvatarIn(vs ...string) predicate.User {
	return predicate.User(sql.FieldIn(FieldAvatar, vs...))
}

// AvatarNotIn applies the NotIn predicate on the "avatar" field.
func AvatarNotIn(vs ...string) predicate.User {
	return predicate.User(sql.FieldNotIn(FieldAvatar, vs...))
}

// AvatarGT applies the GT predicate on the "avatar" field.
func AvatarGT(v string) predicate.User {
	return predicate.User(sql.FieldGT(FieldAvatar, v))
}

// AvatarGTE applies the GTE predicate on the "avatar" field.
func AvatarGTE(v string) predicate.User {
	return predicate.User(sql.FieldGTE(FieldAvatar, v))
}

// AvatarLT applies the LT predicate on the "avatar" field.
func AvatarLT(v string) predicate.User {
	return predicate.User(sql.FieldLT(FieldAvatar, v))
}

// AvatarLTE applies the LTE predicate on the "avatar" field.
func AvatarLTE(v string) predicate.User {
	return predicate.User(sql.FieldLTE(FieldAvatar, v))
}

// AvatarContains applies the Contains predicate on the "avatar" field.
func AvatarContains(v string) predicate.User {
	return predicate.User(sql.FieldContains(FieldAvatar, v))
}

// AvatarHasPrefix applies the HasPrefix predicate on the "avatar" field.
func AvatarHasPrefix(v string) predicate.User {
	return predicate.User(sql.FieldHasPrefix(FieldAvatar, v))
}

// AvatarHasSuffix applies the HasSuffix predicate on the "avatar" field.
func AvatarHasSuffix(v string) predicate.User {
	return predicate.User(sql.FieldHasSuffix(FieldAvatar, v))
}

// AvatarIsNil applies the IsNil predicate on the "avatar" field.
func AvatarIsNil() predicate.User {
	return predicate.User(sql.FieldIsNull(FieldAvatar))
}

// AvatarNotNil applies the NotNil predicate on the "avatar" field.
func AvatarNotNil() predicate.User {
	return predicate.User(sql.FieldNotNull(FieldAvatar))
}

// AvatarEqualFold applies the EqualFold predicate on the "avatar" field.
func AvatarEqualFold(v string) predicate.User {
	return predicate.User(sql.FieldEqualFold(FieldAvatar, v))
}

// AvatarContainsFold applies the ContainsFold predicate on the "avatar" field.
func AvatarContainsFold(v string) predicate.User {
	return predicate.User(sql.FieldContainsFold(FieldAvatar, v))
}

// CreateAtEQ applies the EQ predicate on the "create_at" field.
func CreateAtEQ(v time.Time) predicate.User {
	return predicate.User(sql.FieldEQ(FieldCreateAt, v))
}

// CreateAtNEQ applies the NEQ predicate on the "create_at" field.
func CreateAtNEQ(v time.Time) predicate.User {
	return predicate.User(sql.FieldNEQ(FieldCreateAt, v))
}

// CreateAtIn applies the In predicate on the "create_at" field.
func CreateAtIn(vs ...time.Time) predicate.User {
	return predicate.User(sql.FieldIn(FieldCreateAt, vs...))
}

// CreateAtNotIn applies the NotIn predicate on the "create_at" field.
func CreateAtNotIn(vs ...time.Time) predicate.User {
	return predicate.User(sql.FieldNotIn(FieldCreateAt, vs...))
}

// CreateAtGT applies the GT predicate on the "create_at" field.
func CreateAtGT(v time.Time) predicate.User {
	return predicate.User(sql.FieldGT(FieldCreateAt, v))
}

// CreateAtGTE applies the GTE predicate on the "create_at" field.
func CreateAtGTE(v time.Time) predicate.User {
	return predicate.User(sql.FieldGTE(FieldCreateAt, v))
}

// CreateAtLT applies the LT predicate on the "create_at" field.
func CreateAtLT(v time.Time) predicate.User {
	return predicate.User(sql.FieldLT(FieldCreateAt, v))
}

// CreateAtLTE applies the LTE predicate on the "create_at" field.
func CreateAtLTE(v time.Time) predicate.User {
	return predicate.User(sql.FieldLTE(FieldCreateAt, v))
}

// LastLoginEQ applies the EQ predicate on the "last_login" field.
func LastLoginEQ(v time.Time) predicate.User {
	return predicate.User(sql.FieldEQ(FieldLastLogin, v))
}

// LastLoginNEQ applies the NEQ predicate on the "last_login" field.
func LastLoginNEQ(v time.Time) predicate.User {
	return predicate.User(sql.FieldNEQ(FieldLastLogin, v))
}

// LastLoginIn applies the In predicate on the "last_login" field.
func LastLoginIn(vs ...time.Time) predicate.User {
	return predicate.User(sql.FieldIn(FieldLastLogin, vs...))
}

// LastLoginNotIn applies the NotIn predicate on the "last_login" field.
func LastLoginNotIn(vs ...time.Time) predicate.User {
	return predicate.User(sql.FieldNotIn(FieldLastLogin, vs...))
}

// LastLoginGT applies the GT predicate on the "last_login" field.
func LastLoginGT(v time.Time) predicate.User {
	return predicate.User(sql.FieldGT(FieldLastLogin, v))
}

// LastLoginGTE applies the GTE predicate on the "last_login" field.
func LastLoginGTE(v time.Time) predicate.User {
	return predicate.User(sql.FieldGTE(FieldLastLogin, v))
}

// LastLoginLT applies the LT predicate on the "last_login" field.
func LastLoginLT(v time.Time) predicate.User {
	return predicate.User(sql.FieldLT(FieldLastLogin, v))
}

// LastLoginLTE applies the LTE predicate on the "last_login" field.
func LastLoginLTE(v time.Time) predicate.User {
	return predicate.User(sql.FieldLTE(FieldLastLogin, v))
}

// UpdatedAtEQ applies the EQ predicate on the "updated_at" field.
func UpdatedAtEQ(v time.Time) predicate.User {
	return predicate.User(sql.FieldEQ(FieldUpdatedAt, v))
}

// UpdatedAtNEQ applies the NEQ predicate on the "updated_at" field.
func UpdatedAtNEQ(v time.Time) predicate.User {
	return predicate.User(sql.FieldNEQ(FieldUpdatedAt, v))
}

// UpdatedAtIn applies the In predicate on the "updated_at" field.
func UpdatedAtIn(vs ...time.Time) predicate.User {
	return predicate.User(sql.FieldIn(FieldUpdatedAt, vs...))
}

// UpdatedAtNotIn applies the NotIn predicate on the "updated_at" field.
func UpdatedAtNotIn(vs ...time.Time) predicate.User {
	return predicate.User(sql.FieldNotIn(FieldUpdatedAt, vs...))
}

// UpdatedAtGT applies the GT predicate on the "updated_at" field.
func UpdatedAtGT(v time.Time) predicate.User {
	return predicate.User(sql.FieldGT(FieldUpdatedAt, v))
}

// UpdatedAtGTE applies the GTE predicate on the "updated_at" field.
func UpdatedAtGTE(v time.Time) predicate.User {
	return predicate.User(sql.FieldGTE(FieldUpdatedAt, v))
}

// UpdatedAtLT applies the LT predicate on the "updated_at" field.
func UpdatedAtLT(v time.Time) predicate.User {
	return predicate.User(sql.FieldLT(FieldUpdatedAt, v))
}

// UpdatedAtLTE applies the LTE predicate on the "updated_at" field.
func UpdatedAtLTE(v time.Time) predicate.User {
	return predicate.User(sql.FieldLTE(FieldUpdatedAt, v))
}

// PasswordEQ applies the EQ predicate on the "password" field.
func PasswordEQ(v string) predicate.User {
	return predicate.User(sql.FieldEQ(FieldPassword, v))
}

// PasswordNEQ applies the NEQ predicate on the "password" field.
func PasswordNEQ(v string) predicate.User {
	return predicate.User(sql.FieldNEQ(FieldPassword, v))
}

// PasswordIn applies the In predicate on the "password" field.
func PasswordIn(vs ...string) predicate.User {
	return predicate.User(sql.FieldIn(FieldPassword, vs...))
}

// PasswordNotIn applies the NotIn predicate on the "password" field.
func PasswordNotIn(vs ...string) predicate.User {
	return predicate.User(sql.FieldNotIn(FieldPassword, vs...))
}

// PasswordGT applies the GT predicate on the "password" field.
func PasswordGT(v string) predicate.User {
	return predicate.User(sql.FieldGT(FieldPassword, v))
}

// PasswordGTE applies the GTE predicate on the "password" field.
func PasswordGTE(v string) predicate.User {
	return predicate.User(sql.FieldGTE(FieldPassword, v))
}

// PasswordLT applies the LT predicate on the "password" field.
func PasswordLT(v string) predicate.User {
	return predicate.User(sql.FieldLT(FieldPassword, v))
}

// PasswordLTE applies the LTE predicate on the "password" field.
func PasswordLTE(v string) predicate.User {
	return predicate.User(sql.FieldLTE(FieldPassword, v))
}

// PasswordContains applies the Contains predicate on the "password" field.
func PasswordContains(v string) predicate.User {
	return predicate.User(sql.FieldContains(FieldPassword, v))
}

// PasswordHasPrefix applies the HasPrefix predicate on the "password" field.
func PasswordHasPrefix(v string) predicate.User {
	return predicate.User(sql.FieldHasPrefix(FieldPassword, v))
}

// PasswordHasSuffix applies the HasSuffix predicate on the "password" field.
func PasswordHasSuffix(v string) predicate.User {
	return predicate.User(sql.FieldHasSuffix(FieldPassword, v))
}

// PasswordIsNil applies the IsNil predicate on the "password" field.
func PasswordIsNil() predicate.User {
	return predicate.User(sql.FieldIsNull(FieldPassword))
}

// PasswordNotNil applies the NotNil predicate on the "password" field.
func PasswordNotNil() predicate.User {
	return predicate.User(sql.FieldNotNull(FieldPassword))
}

// PasswordEqualFold applies the EqualFold predicate on the "password" field.
func PasswordEqualFold(v string) predicate.User {
	return predicate.User(sql.FieldEqualFold(FieldPassword, v))
}

// PasswordContainsFold applies the ContainsFold predicate on the "password" field.
func PasswordContainsFold(v string) predicate.User {
	return predicate.User(sql.FieldContainsFold(FieldPassword, v))
}

// And groups predicates with the AND operator between them.
func And(predicates ...predicate.User) predicate.User {
	return predicate.User(sql.AndPredicates(predicates...))
}

// Or groups predicates with the OR operator between them.
func Or(predicates ...predicate.User) predicate.User {
	return predicate.User(sql.OrPredicates(predicates...))
}

// Not applies the not operator on the given predicate.
func Not(p predicate.User) predicate.User {
	return predicate.User(sql.NotPredicates(p))
}
