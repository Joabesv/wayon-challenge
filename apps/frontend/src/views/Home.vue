<template>
  <div class="min-h-screen bg-background">
    <nav class="bg-card shadow-sm border-b">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between h-16">
          <div class="flex items-center">
            <h1 class="text-xl font-semibold text-foreground">
              Sistema de Transferências Financeiras
            </h1>
          </div>
          <div class="flex items-center space-x-4">
            <router-link
              to="/"
              class="text-muted-foreground hover:text-foreground px-3 py-2 rounded-md text-sm font-medium transition-colors"
              :class="{ 'bg-accent text-accent-foreground': $route.name === 'home' }"
            >
              Nova Transferência
            </router-link>
            <router-link
              to="/transfers"
              class="text-muted-foreground hover:text-foreground px-3 py-2 rounded-md text-sm font-medium transition-colors"
              :class="{ 'bg-accent text-accent-foreground': $route.name === 'transfers' }"
            >
              Extrato
            </router-link>
          </div>
        </div>
      </div>
    </nav>

    <main class="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
      <div class="px-4 py-6 sm:px-0">
        <Card class="max-w-2xl mx-auto">
          <CardHeader>
            <CardTitle class="text-2xl">
              Agendar Nova Transferência
            </CardTitle>
          </CardHeader>
          
          <form @submit="onSubmit">
            <CardContent>
              <div class="space-y-6">
                <div class="grid grid-cols-1 gap-6 sm:grid-cols-2">
                  <FormField v-slot="{ componentField }" name="sourceAccount">
                    <FormItem>
                      <FormLabel>Conta de Origem</FormLabel>
                      <FormControl>
                        <Input
                          placeholder="0000000000"
                          maxlength="10"
                          v-bind="componentField"
                        />
                      </FormControl>
                      <FormDescription>10 dígitos</FormDescription>
                      <FormMessage />
                    </FormItem>
                  </FormField>

                  <FormField v-slot="{ componentField }" name="destinationAccount">
                    <FormItem>
                      <FormLabel>Conta de Destino</FormLabel>
                      <FormControl>
                        <Input
                          placeholder="0000000000"
                          maxlength="10"
                          v-bind="componentField"
                        />
                      </FormControl>
                      <FormDescription>10 dígitos</FormDescription>
                      <FormMessage />
                    </FormItem>
                  </FormField>
                </div>

                <div class="grid grid-cols-1 gap-6 sm:grid-cols-2">
                  <FormField v-slot="{ componentField }" name="transferAmount">
                    <FormItem>
                      <FormLabel>Valor da Transferência</FormLabel>
                      <FormControl>
                        <Input
                          type="number"
                          placeholder="0.01"
                          :min="0.01"
                          :step="0.01"
                          v-bind="componentField"
                        />
                      </FormControl>
                      <FormDescription class="mb-3">Valor mínimo: R$ 0,01</FormDescription>
                      <FormMessage />
                    </FormItem>
                  </FormField>

                  <FormField v-slot="{ componentField }" name="transferDate">
                    <FormItem>
                      <FormLabel>Data da Transferência</FormLabel>
                      <FormControl>
                        <DatePicker
                          v-bind="componentField"
                          placeholder="Selecione a data da transferência"
                          :disabled-date="(date) => {
                            const today = new Date()
                            today.setHours(0, 0, 0, 0)
                            return date < today
                          }"
                        />
                      </FormControl >
                      <FormMessage class="mb-3"/>
                    </FormItem>
                  </FormField>
                </div>

                <Alert v-if="calculatedFee !== null" variant="default" class="border-blue-200 bg-blue-50 mb-2 relative">
                  <Button 
                    variant="ghost" 
                    size="sm" 
                    class="absolute top-2 right-2 h-6 w-6 p-0 hover:bg-blue-100"
                    @click="calculatedFee = null"
                  >
                    <span class="sr-only">Fechar</span>
                    <X class="h-4 w-4" />
                  </Button>
                  <AlertTitle class="font-medium text-blue-800 pr-8">Resumo da Transferência</AlertTitle>
                  <AlertDescription class="text-sm text-blue-700 space-y-1">
                    <p>Valor: {{ formatCurrency(typeof values.transferAmount === 'string' ? parseFloat(values.transferAmount) || 0 : values.transferAmount || 0) }}</p>
                    <p>Taxa: {{ formatCurrency(calculatedFee) }}</p>
                    <p class="font-semibold border-t pt-1">
                      Total: {{ formatCurrency((typeof values.transferAmount === 'string' ? parseFloat(values.transferAmount) || 0 : values.transferAmount || 0) + calculatedFee) }}
                    </p>
                  </AlertDescription>
                </Alert>
              </div>
            </CardContent>

            <CardFooter class="flex flex-col space-y-4">
              <!-- Step indicator -->
              <div class="w-full flex items-center justify-center space-x-4 text-sm">
                <div class="flex items-center space-x-2">
                  <div class="w-6 h-6 rounded-full bg-primary text-primary-foreground flex items-center justify-center text-xs font-medium">
                    1
                  </div>
                  <span class="text-muted-foreground">Preencher dados</span>
                </div>
                <div class="w-8 h-px bg-border"></div>
                <div class="flex items-center space-x-2">
                  <div class="w-6 h-6 rounded-full flex items-center justify-center text-xs font-medium"
                       :class="calculatedFee !== null ? 'bg-primary text-primary-foreground' : 'bg-muted text-muted-foreground'">
                    2
                  </div>
                  <span :class="calculatedFee !== null ? 'text-foreground' : 'text-muted-foreground'">Calcular taxa</span>
                </div>
                <div class="w-8 h-px bg-border"></div>
                <div class="flex items-center space-x-2">
                  <div class="w-6 h-6 rounded-full flex items-center justify-center text-xs font-medium"
                       :class="calculatedFee !== null ? 'bg-primary text-primary-foreground' : 'bg-muted text-muted-foreground'">
                    3
                  </div>
                  <span :class="calculatedFee !== null ? 'text-foreground' : 'text-muted-foreground'">Confirmar transferência</span>
                </div>
              </div>

              <!-- Action buttons -->
              <div class="flex justify-between space-x-4 w-full">
                <Button
                  type="button"
                  @click="calculateFee"
                  :disabled="!meta.valid"
                  variant="outline"
                  class="flex-1"
                >
                  {{ calculatedFee === null ? 'Calcular Taxa' : 'Recalcular Taxa' }}
                </Button>

                <Button
                  type="submit"
                  :disabled="!meta.valid || calculatedFee === null"
                  class="flex-1"
                  :class="calculatedFee === null ? 'opacity-50' : ''"
                >
                  {{ calculatedFee === null ? 'Confirme a taxa primeiro' : 'Confirmar Transferência' }}
                </Button>              
              </div>
            </CardFooter>
          </form>
        </Card>
      </div>
    </main>

    <!-- Toaster component for notifications -->
    <Toaster position="top-right" />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useForm } from 'vee-validate'
import { toTypedSchema } from '@vee-validate/zod'
import { toast, Toaster } from 'vue-sonner'
import { X, Info } from 'lucide-vue-next'

import { useCreateTransfer, useCalculateFee } from '@/services/queries'
import { transferFormSchema } from '@/lib/schemas'
import { Card, CardContent, CardFooter, CardHeader, CardTitle } from '@/components/ui/card'
import {
  FormField,
  FormItem,
  FormLabel,
  FormControl,
  FormDescription,
  FormMessage,
} from '@/components/ui/form'
import { Input } from '@/components/ui/input'
import DatePicker from '@/components/ui/DatePicker.vue'
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert'
import { Button } from '@/components/ui/button'

const router = useRouter()

const { handleSubmit, values, meta, resetForm } = useForm({
  validationSchema: toTypedSchema(transferFormSchema),
  initialValues: {
    sourceAccount: '',
    destinationAccount: '',
    transferAmount: '0.01',
    transferDate: undefined as Date | undefined
  }
})

const { mutateAsync: createTransferMutation } = useCreateTransfer()
const { mutateAsync: calculateFeeMutation } = useCalculateFee()

const calculatedFee = ref<number | null>(null)

const calculateFee = async () => {
  if (!meta.value.valid) {
    toast.error('Preencha todos os campos corretamente para calcular a taxa')
    return
  }

  try {
    const amount = typeof values.transferAmount === 'string' ? parseFloat(values.transferAmount) : values.transferAmount
    
    const fee = await calculateFeeMutation({
      transferAmount: amount || 0,
      transferDate: values.transferDate!.toISOString().split('T')[0]
    })
    calculatedFee.value = fee
  } catch (error) {
    toast.error('Erro ao calcular taxa. Tente novamente.')
    console.error('Fee calculation error:', error)
  }
}

const onSubmit = handleSubmit(async (formValues) => {
  if (calculatedFee.value === null) {
    toast.error('Calcule a taxa antes de agendar a transferência')
    return
  }

  try {
    const amount = typeof formValues.transferAmount === 'string' ? parseFloat(formValues.transferAmount) : formValues.transferAmount
    
    await createTransferMutation({
      sourceAccount: formValues.sourceAccount,
      destinationAccount: formValues.destinationAccount,
      transferAmount: amount,
      transferDate: formValues.transferDate!.toISOString().split('T')[0]
    })

    toast.success('Transferência agendada com sucesso!', {
      description: `Valor: ${formatCurrency(amount)} + Taxa: ${formatCurrency(calculatedFee.value)}`
    })

    calculatedFee.value = null
    resetForm()

    setTimeout(() => {
      router.push('/transfers')
    }, 1500)
  } catch (error) {
    toast.error('Erro ao agendar transferência. Tente novamente.')
    console.error('Transfer creation error:', error)
  }
})

const formatCurrency = (value: number): string => {
  return new Intl.NumberFormat('pt-BR', {
    style: 'currency',
    currency: 'BRL'
  }).format(value)
}
</script>
