import { ofetch } from 'ofetch'

const getApiBaseUrl = () => {
  if (import.meta.env.VITE_API_BASE_URL) {
    return import.meta.env.VITE_API_BASE_URL
  }
  
  if (typeof window !== 'undefined') {
    const hostname = window.location.hostname
    const protocol = window.location.protocol
    // If accessing via IP address (mobile), use the same IP for API
    return `${protocol}//${hostname}:8080/api`
  }
  
  return 'http://localhost:8080/api'
}

const api = ofetch.create({
  baseURL: getApiBaseUrl(),
  retry: 2,
  retryDelay: 1000,
  onRequestError({ error }) {
    console.error('API Request Error:', error)
  },
  onResponseError({ response, error }) {
    console.error('API Response Error:', response?.status, error)
  }
})

type BaseApiResponse<T> = {
  data: T | null
  status: 'SUCCESS' | 'ERROR'
  message: string | null
}

export interface TransferRequest {
  sourceAccount: string
  destinationAccount: string
  transferAmount: number
  transferDate: string
}

export interface TransferResponse {
  id: number
  sourceAccount: string
  destinationAccount: string
  transferAmount: number
  fee: number
  transferDate: string
  scheduleDate: string
}

export interface FeeCalculationRequest {
  transferAmount: number
  transferDate: string
}

export interface FeeCalculationResponse {
  fee: number
}

export const transferApi = {
  scheduleTransfer: (data: TransferRequest) => 
    api<BaseApiResponse<TransferResponse>>('/transfers', {
      method: 'POST',
      body: data,
    }),
  
  getAllTransfers: () => 
    api<BaseApiResponse<TransferResponse[]>>('/transfers'),

  calculateFee: (data: FeeCalculationRequest) => 
    api<BaseApiResponse<FeeCalculationResponse>>('/transfers/calculate-fee', {
      method: 'POST',
      body: data,
    }),
}

export default api
