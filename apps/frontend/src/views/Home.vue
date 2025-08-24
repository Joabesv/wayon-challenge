<template>
  <div class="min-h-screen bg-background">
    <nav class="bg-card shadow-sm border-b">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between items-center h-16">
          <div class="flex items-center">
            <h1 class="text-lg sm:text-xl font-semibold text-foreground truncate">
              <span class="hidden sm:inline">Sistema de Transferências Financeiras</span>
              <span class="sm:hidden">Transferências</span>
            </h1>
          </div>
          <div class="flex items-center space-x-2 sm:space-x-4">
            <router-link
              to="/"
              class="text-muted-foreground hover:text-foreground px-2 sm:px-3 py-2 rounded-md text-xs sm:text-sm font-medium transition-colors"
              :class="{ 'bg-accent text-accent-foreground': $route.name === 'home' }"
            >
              <span class="hidden sm:inline">Nova Transferência</span>
              <span class="sm:hidden">Nova</span>
            </router-link>
            <router-link
              to="/transfers"
              class="text-muted-foreground hover:text-foreground px-2 sm:px-3 py-2 rounded-md text-xs sm:text-sm font-medium transition-colors"
              :class="{ 'bg-accent text-accent-foreground': $route.name === 'transfers' }"
            >
              Extrato
            </router-link>
          </div>
        </div>
      </div>
    </nav>

    <main class="max-w-7xl mx-auto py-4 sm:py-6 px-4 sm:px-6 lg:px-8">
      <div class="sm:px-0">
        <Card class="max-w-2xl mx-auto">
          <CardHeader class="pb-4 sm:pb-6">
            <CardTitle class="text-xl sm:text-2xl text-center sm:text-left">
              Agendar Nova Transferência
            </CardTitle>
          </CardHeader>
          
          <form @submit="onSubmit">
            <CardContent class="px-4 sm:px-6">
              <div class="space-y-4 sm:space-y-6">
                <div class="grid grid-cols-1 gap-4 sm:gap-6 md:grid-cols-2">
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

                <div class="grid grid-cols-1 gap-4 sm:gap-6 md:grid-cols-2">
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

            <CardFooter class="flex flex-col space-y-4 px-4 sm:px-6">
              <!-- Step indicator -->
              <div class="w-full">
                <!-- Mobile version - Vertical layout -->
                <div class="block mt-2 sm:hidden space-y-3">
                  <div class="flex items-center space-x-3">
                    <div class="w-6 h-6 rounded-full bg-primary text-primary-foreground flex items-center justify-center text-xs font-medium">
                      1
                    </div>
                    <span class="text-muted-foreground text-sm">Preencher dados</span>
                  </div>
                  <div class="flex items-center space-x-3">
                    <div class="w-6 h-6 rounded-full flex items-center justify-center text-xs font-medium"
                         :class="calculatedFee !== null ? 'bg-primary text-primary-foreground' : 'bg-muted text-muted-foreground'">
                      2
                    </div>
                    <span :class="calculatedFee !== null ? 'text-foreground' : 'text-muted-foreground'" class="text-sm">Calcular taxa</span>
                  </div>
                  <div class="flex items-center space-x-3">
                    <div class="w-6 h-6 rounded-full flex items-center justify-center text-xs font-medium"
                         :class="calculatedFee !== null ? 'bg-primary text-primary-foreground' : 'bg-muted text-muted-foreground'">
                      3
                    </div>
                    <span :class="calculatedFee !== null ? 'text-foreground' : 'text-muted-foreground'" class="text-sm">Confirmar transferência</span>
                  </div>
                </div>
                
                <!-- Desktop version - Horizontal layout -->
                <div class="hidden sm:flex items-center justify-center space-x-4 text-sm">
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
              </div>

              <!-- Action buttons -->
              <div class="flex flex-col sm:flex-row sm:justify-between space-y-3 sm:space-y-0 sm:space-x-4 w-full">
                <Button
                  type="button"
                  @click="calculateFee"
                  :disabled="!meta.valid || isCalculatingFee"
                  variant="outline"
                  class="w-full sm:flex-1"
                >
                  <div v-if="isCalculatingFee" class="flex items-center justify-center">
                    <div class="animate-spin rounded-full h-4 w-4 border-b-2 border-current mr-2"></div>
                    Calculando...
                  </div>
                  <span v-else>{{ calculatedFee === null ? 'Calcular Taxa' : 'Recalcular Taxa' }}</span>
                </Button>

                <Button
                  type="submit"
                  :disabled="!meta.valid || calculatedFee === null || isCreatingTransfer"
                  class="w-full sm:flex-1"
                  :class="calculatedFee === null ? 'opacity-50' : ''"
                >
                  <div v-if="isCreatingTransfer" class="flex items-center justify-center">
                    <div class="animate-spin rounded-full h-4 w-4 border-b-2 border-current mr-2"></div>
                    <span class="hidden sm:inline">Agendando...</span>
                    <span class="sm:hidden">Agendando...</span>
                  </div>
                  <span v-else>
                    <span class="hidden sm:inline">{{ calculatedFee === null ? 'Confirme a taxa primeiro' : 'Confirmar Transferência' }}</span>
                    <span class="sm:hidden">{{ calculatedFee === null ? 'Confirme a taxa' : 'Confirmar' }}</span>
                  </span>
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
import { X } from 'lucide-vue-next'

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

const { mutateAsync: createTransferMutation, isPending: isCreatingTransfer } = useCreateTransfer()
const { mutateAsync: calculateFeeMutation, isPending: isCalculatingFee } = useCalculateFee()

const calculatedFee = ref<number | null>(null)

const calculateFee = async () => {
  if (!meta.value.valid) {
    toast.error('Preencha todos os campos corretamente para calcular a taxa', {
      duration: 3000,
      position: 'top-center'
    })
    return
  }

  try {
    const amount = typeof values.transferAmount === 'string' ? parseFloat(values.transferAmount) : values.transferAmount
    
    const fee = await calculateFeeMutation({
      transferAmount: amount || 0,
      transferDate: values.transferDate!.toISOString().split('T')[0]
    })
    calculatedFee.value = fee
  } catch (error: any) {
    const errorMessage = error.message || 'Erro ao calcular taxa. Tente novamente.'
    toast.error(errorMessage, {
      duration: 4000,
      position: 'top-center' // Better for mobile
    })
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
      description: `Valor: ${formatCurrency(amount)} + Taxa: ${formatCurrency(calculatedFee.value)}`,
      duration: 3000,
      position: 'top-center' // Better for mobile
    })

    calculatedFee.value = null
    resetForm()

    setTimeout(() => {
      router.push('/transfers')
    }, 1500)
  } catch (error: any) {
    const errorMessage = error.message || 'Erro ao agendar transferência. Tente novamente.'
    toast.error(errorMessage, {
      duration: 4000,
      position: 'top-center' // Better for mobile
    })
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
