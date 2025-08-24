import { z } from 'zod'

export const transferFormSchema = z.object({
  sourceAccount: z
    .string()
    .refine((val) => val === '' || /^\d{10}$/.test(val), 'Conta de origem deve ter exatamente 10 dígitos'),
  
  destinationAccount: z
    .string()
    .refine((val) => val === '' || /^\d{10}$/.test(val), 'Conta de destino deve ter exatamente 10 dígitos'),
  
  transferAmount: z
    .union([z.string(), z.number()])
    .transform((val) => typeof val === 'string' ? parseFloat(val) || 0 : val)
    .pipe(z.number().min(0.01, 'Valor deve ser maior que zero').max(999999.99, 'Valor muito alto')),
  
  transferDate: z
    .union([z.string(), z.date()])
    .transform((val) => {
      if (typeof val === 'string') {
        return val ? new Date(val) : undefined
      }
      return val
    })
    .pipe(
      z.date({
        required_error: 'Data da transferência é obrigatória'
      })
      .refine((date: Date) => {
        const today = new Date()
        today.setHours(0, 0, 0, 0)
        return date >= today
      }, 'Data da transferência não pode ser anterior à data atual')
    )
    .optional()
})


export type TransferFormData = z.infer<typeof transferFormSchema>

export const feeCalculationSchema = z.object({
  amount: z.number().min(0.01, 'Valor deve ser maior que zero'),
  transferDate: z.date()
})

export type FeeCalculationData = z.infer<typeof feeCalculationSchema>
