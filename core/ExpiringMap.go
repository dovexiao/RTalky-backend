package core

import (
	"container/heap"
	"golang.org/x/exp/constraints"
	"sync"
	"time"

	"github.com/logrusorgru/rbtree"
)

// entry 表示存储的键值对及其过期时间
type entry[K constraints.Ordered, V any] struct {
	key        K
	value      V
	expireTime time.Time
	index      int // 在堆中的索引
}

// expireHeap 是一个最小堆，按过期时间排序
type expireHeap[K constraints.Ordered, V any] []*entry[K, V]

func (h *expireHeap[K, V]) Len() int {
	return len(*h)
}

func (h *expireHeap[K, V]) Less(i, j int) bool {
	lhs := (*h)[i].expireTime
	rhs := (*h)[j].expireTime
	return lhs.Before(rhs)
}

func (h *expireHeap[K, V]) Swap(i, j int) {
	(*h)[i], (*h)[j] = (*h)[j], (*h)[i]
	(*h)[i].index = i
	(*h)[j].index = j
}

func (h *expireHeap[K, V]) Push(x any) {
	n := len(*h)
	item := x.(*entry[K, V])
	item.index = n
	*h = append(*h, item)
}

func (h *expireHeap[K, V]) Pop() any {
	old := *h
	n := len(old)
	item := old[n-1]
	item.index = -1 // for safety
	*h = old[0 : n-1]
	return item
}

// ExpiringMap 是支持过期时间的键值对容器
type ExpiringMap[K constraints.Ordered, V any] struct {
	mu   sync.RWMutex
	tree *rbtree.TreeThreadSafe[K, *entry[K, V]]
	heap expireHeap[K, V]
}

// NewExpiringMap 创建一个新的 ExpiringMap
func NewExpiringMap[K constraints.Ordered, V any]() *ExpiringMap[K, V] {
	return &ExpiringMap[K, V]{
		tree: rbtree.NewThreadSafe[K, *entry[K, V]](),
		heap: make(expireHeap[K, V], 0),
	}
}

// Set 添加或更新键值对，并设置过期时间
func (m *ExpiringMap[K, V]) Set(key K, value V, ttl time.Duration) {
	m.mu.Lock()
	defer m.mu.Unlock()

	m.removeExpired()

	expireTime := time.Now().Add(ttl)
	if e, ok := m.tree.GetEx(key); ok {
		// 更新已有键
		e.value = value
		e.expireTime = expireTime
		heap.Fix(&m.heap, e.index)
	} else {
		// 添加新键
		e := &entry[K, V]{
			key:        key,
			value:      value,
			expireTime: expireTime,
		}
		m.tree.Set(key, e)
		heap.Push(&m.heap, e)
	}
}

// Get 根据键获取值，如果键存在且未过期，返回值和 true；否则返回零值和 false
func (m *ExpiringMap[K, V]) Get(key K) (V, bool) {
	m.mu.RLock()
	defer m.mu.RUnlock()

	m.removeExpired()

	if e, ok := m.tree.GetEx(key); ok {
		if e.expireTime.After(time.Now()) {
			return e.value, true
		}
	}
	var zero V
	return zero, false
}

// Delete 删除指定键的键值对
func (m *ExpiringMap[K, V]) Delete(key K) {
	m.mu.Lock()
	defer m.mu.Unlock()

	m.removeExpired()

	if e, ok := m.tree.GetEx(key); ok {
		heap.Remove(&m.heap, e.index)
		m.tree.Del(key)
	}
}

// removeExpired 移除所有已过期的键值对
func (m *ExpiringMap[K, V]) removeExpired() {
	now := time.Now()
	for len(m.heap) > 0 {
		e := m.heap[0]
		if e.expireTime.After(now) {
			break
		}
		heap.Remove(&m.heap, e.index)
		m.tree.Del(e.key)
	}
}

// Len 返回当前未过期的键值对数量
func (m *ExpiringMap[K, V]) Len() int {
	m.mu.RLock()
	defer m.mu.RUnlock()

	m.removeExpired()
	return m.tree.Len()
}
