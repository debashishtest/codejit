/* eslint-disable react-refresh/only-export-components */
import { createContext, useContext, useState, type ReactNode } from 'react'
import { api } from '../api/client'

type AuthContextValue = { token: string | null; username: string | null; login: (username: string, password: string) => Promise<void>; register: (name: string, email: string, password: string) => Promise<void>; logout: () => void }
const AuthContext = createContext<AuthContextValue | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState(() => localStorage.getItem('forge_token'))
  const [username, setUsername] = useState(() => localStorage.getItem('forge_username'))
  const setSession = (nextToken: string, nextUsername: string) => { localStorage.setItem('forge_token', nextToken); localStorage.setItem('forge_username', nextUsername); setToken(nextToken); setUsername(nextUsername) }
  const login = async (email: string, password: string) => { const response = await api.login(email, password); setSession(response.token, email) }
  const register = async (name: string, email: string, password: string) => { const response = await api.register(name, email, password); setSession(response.token, email) }
  const logout = () => { localStorage.removeItem('forge_token'); localStorage.removeItem('forge_username'); setToken(null); setUsername(null) }
  const value = { token, username, login, register, logout }
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
export function useAuth() { const context = useContext(AuthContext); if (!context) throw new Error('useAuth must be used inside AuthProvider'); return context }
