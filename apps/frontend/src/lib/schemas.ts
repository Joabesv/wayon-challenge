import { z } from 'zod'

export const transferFormSchema = z.object({
  sourceAccount: z
    .string()
    .min(1, 'Conta de origem é obrigatória')
    .regex(/^\d{10}$/, 'Conta de origem deve ter exatamente 10 dígitos'),
  
  destinationAccount: z
    .string()
    .min(1, 'Conta de destino é obrigatória')
    .regex(/^\d{10}$/, 'Conta de destino deve ter exatamente 10 dígitos'),
  
  transferAmount: z
    .number()
    .min(0.01, 'Valor deve ser maior que zero')
    .max(999999.99, 'Valor muito alto'),
  
  transferDate: z
    .date({
      required_error: 'Data da transferência é obrigatória'
    })
    .refine((date: Date) => {
      const today = new Date()
      today.setHours(0, 0, 0, 0)
      return date >= today
    }, 'Data da transferência não pode ser anterior à data atual')
})


export type TransferFormData = z.infer<typeof transferFormSchema>

export const feeCalculationSchema = z.object({
  amount: z.number().min(0.01, 'Valor deve ser maior que zero'),
  transferDate: z.date()
})

export type FeeCalculationData = z.infer<typeof feeCalculationSchema>
