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
                  <FormField>
                    <Label for="sourceAccount">
                      Conta de Origem
                    </Label>
                    <Input
                      id="sourceAccount"
                      v-model="sourceAccount"
                      placeholder="0000000000"
                      maxlength="10"
                      :class="shouldShowError('sourceAccount') ? 'border-destructive' : ''"
                    />
                    <FormDescription>10 dígitos</FormDescription>
                    <FormMessage :message="shouldShowError('sourceAccount') ? errors.sourceAccount : ''" />
                  </FormField>

                  <FormField>
                    <Label for="destinationAccount">
                      Conta de Destino
                    </Label>
                    <Input
                      id="destinationAccount"
                      v-model="destinationAccount"
                      placeholder="0000000000"
                      maxlength="10"
                      :class="shouldShowError('destinationAccount') ? 'border-destructive' : ''"
                    />
                    <FormDescription>10 dígitos</FormDescription>
                    <FormMessage :message="shouldShowError('destinationAccount') ? errors.destinationAccount : ''" />
                  </FormField>
                </div>

                <div class="grid grid-cols-1 gap-6 sm:grid-cols-2">
                  <FormField>
                    <Label for="transferAmount">
                      Valor da Transferência
                    </Label>
                    <NumberField 
                      v-model="transferAmount"
                      :min="0.01"
                      :step="0.01"
                      :format-options="{
                        style: 'currency',
                        currency: 'BRL'
                      }"
                    >
                      <NumberFieldContent />
                    </NumberField>
                    <FormMessage :message="shouldShowError('transferAmount') ? errors.transferAmount : ''" />
                  </FormField>

                  <FormField>
                    <Label for="transferDate">
                      Data da Transferência
                    </Label>
                    <DatePicker
                      v-model="transferDate"
                      :min-date="today.toISOString().split('T')[0]"
                      placeholder="Selecione uma data"
                      :class="shouldShowError('transferDate') ? 'border-destructive' : ''"
                    />
                    <FormMessage :message="shouldShowError('transferDate') ? errors.transferDate : ''" />
                  </FormField>
                </div>

                <Alert v-if="calculatedFee !== null" variant="default" class="border-blue-200 bg-blue-50">
                  <div class="space-y-2">
                    <h4 class="font-medium text-blue-800">Resumo da Transferência</h4>
                    <div class="text-sm text-blue-700 space-y-1">
                      <p>Valor: {{ formatCurrency(transferAmount ?? 0) }}</p>
                      <p>Taxa: {{ formatCurrency(calculatedFee) }}</p>
                      <p class="font-semibold border-t pt-1">
                        Total: {{ formatCurrency((transferAmount ?? 0) + calculatedFee) }}
                      </p>
                    </div>
                  </div>
                </Alert>
              </div>
            </CardContent>

            <CardFooter class="flex justify-end space-x-3">
              <Button
                type="button"
                variant="outline"
                @click="handleCalculateFee"
                :disabled="!canCalculateFee || isCalculatingFee"
              >
                {{ isCalculatingFee ? 'Calculando...' : 'Calcular Taxa' }}
              </Button>
              <Button
                type="submit"
                :disabled="!isValid || calculatedFee === null || isCreatingTransfer"
              >
                {{ isCreatingTransfer ? 'Agendando...' : 'Agendar Transferência' }}
              </Button>
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
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useForm } from 'vee-validate'
import { toTypedSchema } from '@vee-validate/zod'
import { toast, Toaster } from 'vue-sonner'

import Card from '@/components/ui/Card.vue'
import CardHeader from '@/components/ui/CardHeader.vue'
import CardTitle from '@/components/ui/CardTitle.vue'
import CardContent from '@/components/ui/CardContent.vue'
import CardFooter from '@/components/ui/CardFooter.vue'
import Button from '@/components/ui/Button.vue'
import Input from '@/components/ui/Input.vue'
import Label from '@/components/ui/Label.vue'
import Alert from '@/components/ui/Alert.vue'
import NumberField from '@/components/ui/NumberField.vue'
import NumberFieldContent from '@/components/ui/NumberFieldContent.vue'
import DatePicker from '@/components/ui/DatePicker.vue'
import FormField from '@/components/ui/FormField.vue'
import FormMessage from '@/components/ui/FormMessage.vue'
import FormDescription from '@/components/ui/FormDescription.vue'

import { useCreateTransfer, useCalculateFee } from '@/services/queries'
import { transferFormSchema } from '@/lib/schemas'

const router = useRouter()

// Use vee-validate's recommended approach - simpler and more reliable
const { handleSubmit, values, errors, setFieldValue } = useForm({
  validationSchema: toTypedSchema(transferFormSchema),
  initialValues: {
    sourceAccount: '',
    destinationAccount: '',
    transferAmount: 0.01,
    transferDate: undefined
  }
})

const sourceAccount = computed({
  get: () => values.sourceAccount,
  set: (value: string) => setFieldValue('sourceAccount', value)
})

const destinationAccount = computed({
  get: () => values.destinationAccount,
  set: (value: string) => setFieldValue('destinationAccount', value)
})

const transferAmount = computed({
  get: () => values.transferAmount,
  set: (value: number) => setFieldValue('transferAmount', value)
})

const transferDate = computed({
  get: () => values.transferDate ? values.transferDate.toISOString().split('T')[0] : '',
  set: (value: string) => setFieldValue('transferDate', value ? new Date(value) : undefined)
})

// Simplify - remove custom touched logic and use vee-validate's built-in state
const shouldShowError = (fieldName: keyof typeof errors.value) => {
  return errors.value[fieldName]
}

const { mutateAsync: createTransferMutation, isPending: isCreatingTransfer } = useCreateTransfer()
const { mutateAsync: calculateFeeMutation, isPending: isCalculatingFee } = useCalculateFee()

const calculatedFee = ref<number | null>(null)
const today = new Date()

const canCalculateFee = computed(() => {
  return (transferAmount.value ?? 0) >= 0.01 && values.transferDate !== undefined
})

const isValid = computed(() => {
  // Simple validation - just check if fields have valid values and no errors
  const hasValidSourceAccount = (sourceAccount.value || '').length === 10
  const hasValidDestinationAccount = (destinationAccount.value || '').length === 10  
  const hasValidAmount = (transferAmount.value ?? 0) >= 0.01
  const hasValidDate = values.transferDate !== undefined
  
  const hasNoErrors = Object.keys(errors.value).length === 0
  
  return hasValidSourceAccount && 
         hasValidDestinationAccount && 
         hasValidAmount && 
         hasValidDate &&
         hasNoErrors
})

const handleCalculateFee = async () => {
  if (!canCalculateFee.value) {
    toast.error('Preencha o valor e a data para calcular a taxa')
    return
  }

  try {
    const fee = await calculateFeeMutation({
      transferAmount: transferAmount.value ?? 0,
      transferDate: values.transferDate!.toISOString().split('T')[0]
    })
    calculatedFee.value = fee
    toast.success('Taxa calculada com sucesso!')
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
    await createTransferMutation({
      sourceAccount: formValues.sourceAccount,
      destinationAccount: formValues.destinationAccount,
      transferAmount: formValues.transferAmount,
      transferDate: formValues.transferDate!.toISOString().split('T')[0]
    })

    toast.success('Transferência agendada com sucesso!', {
      description: `Valor: ${formatCurrency(formValues.transferAmount)} + Taxa: ${formatCurrency(calculatedFee.value)}`
    })

    calculatedFee.value = null
    
    // Reset form manually since we're not using the resetForm from destructuring
    setFieldValue('sourceAccount', '')
    setFieldValue('destinationAccount', '')
    setFieldValue('transferAmount', 0.01)
    setFieldValue('transferDate', undefined)

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
