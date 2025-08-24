import { ofetch } from 'ofetch'

const baseURL = import.meta.env.BASE_URL ?? 'http://localhost:8080/api'

const api = ofetch.create({
  baseURL,
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
