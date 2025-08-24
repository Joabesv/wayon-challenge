import { useMutation, useQuery, useQueryClient } from '@tanstack/vue-query'
import { transferApi, type TransferRequest, type FeeCalculationRequest } from './api'

export const transferKeys = {
  all: ['transfers'] as const,
  lists: () => [...transferKeys.all, 'list'] as const,
  list: (filters: Record<string, any>) => [...transferKeys.lists(), { filters }] as const,
  details: () => [...transferKeys.all, 'detail'] as const,
  detail: (id: number) => [...transferKeys.details(), id] as const,
  fee: (params: FeeCalculationRequest) => [...transferKeys.all, 'fee', params] as const,
}

export function useTransfers() {
  return useQuery({
    queryKey: transferKeys.lists(),
    queryFn: async () => {
      const response = await transferApi.getAllTransfers()
      return response.data ?? []
    },
    staleTime: 1000 * 60 * 5, // Consider data fresh for 5 minutes
    gcTime: 1000 * 60 * 30, // Keep in cache for 30 minutes
  })
}

export function useFeeCalculation() {
  return useMutation({
    mutationFn: async (params: FeeCalculationRequest) => {
      const response = await transferApi.calculateFee(params)
      return response.data
    },
  })
}

export function useCalculateFee() {
  return useMutation({
    mutationFn: async (params: FeeCalculationRequest) => {
      const response = await transferApi.calculateFee(params)
      return response.data?.fee ?? 0
    },
  })
}

export function useCreateTransfer() {
  const queryClient = useQueryClient()
  
  return useMutation({
    mutationFn: async (transferData: TransferRequest) => {
      const response = await transferApi.scheduleTransfer(transferData)
      return response.data
    },
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: transferKeys.lists()
      })
    },
  })
}

export function usePrefetchTransfers() {
  const queryClient = useQueryClient()
  
  return () => {
    queryClient.prefetchQuery({
      queryKey: transferKeys.lists(),
      queryFn: async () => {
        const response = await transferApi.getAllTransfers()
        return response.data ?? []
      },
      staleTime: 1000 * 60 * 5,
    })
  }
}
