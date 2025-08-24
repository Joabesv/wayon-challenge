<template>
  <div class="min-h-screen bg-gray-50">
    <nav class="bg-white shadow-sm border-b">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between h-16">
          <div class="flex items-center">
            <h1 class="text-xl font-semibold text-gray-900">
              Sistema de Transferências Financeiras
            </h1>
          </div>
          <div class="flex items-center space-x-4">
            <router-link
              to="/"
              class="text-gray-600 hover:text-gray-900 px-3 py-2 rounded-md text-sm font-medium"
              :class="{ 'bg-gray-100 text-gray-900': $route.name === 'home' }"
            >
              Nova Transferência
            </router-link>
            <router-link
              to="/transfers"
              class="text-gray-600 hover:text-gray-900 px-3 py-2 rounded-md text-sm font-medium"
              :class="{ 'bg-gray-100 text-gray-900': $route.name === 'transfers' }"
            >
              Extrato
            </router-link>
          </div>
        </div>
      </div>
    </nav>

    <main class="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
      <div class="px-4 py-6 sm:px-0">
        <Card class="max-w-6xl mx-auto">
          <CardHeader>
            <CardTitle class="text-2xl">
              Extrato de Transferências
            </CardTitle>
          </CardHeader>
          
          <CardContent>
            <div v-if="isLoading" class="text-center py-8">
              <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-primary mx-auto mb-4"></div>
              <p class="text-muted-foreground">Carregando transferências...</p>
            </div>

            <Alert v-else-if="isError" variant="destructive">
              <p>Erro ao carregar transferências. Tente novamente.</p>
              <Button 
                variant="outline" 
                size="sm" 
                class="mt-2"
                @click="refetch"
              >
                Tentar novamente
              </Button>
            </Alert>

            <div v-else-if="!transfers?.length" class="text-center py-8 space-y-4">
              <div class="text-6xl text-muted-foreground mb-4">📋</div>
              <h3 class="text-lg font-medium text-foreground mb-2">
                Nenhuma transferência encontrada
              </h3>
              <p class="text-muted-foreground mb-6">
                Você ainda não agendou nenhuma transferência.
              </p>
              <Button @click="$router.push('/')">
                Agendar primeira transferência
              </Button>
            </div>

            <div v-else class="space-y-6">
              <div class="overflow-hidden shadow ring-1 ring-black ring-opacity-5 md:rounded-lg">
                <table class="min-w-full divide-y divide-gray-300">
                  <thead class="bg-gray-50">
                    <tr>
                      <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        ID
                      </th>
                      <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Conta Origem
                      </th>
                      <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Conta Destino
                      </th>
                      <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Valor
                      </th>
                      <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Taxa
                      </th>
                      <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Data Transferência
                      </th>
                      <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Status
                      </th>
                    </tr>
                  </thead>
                  <tbody class="bg-white divide-y divide-gray-200">
                    <tr v-for="transfer in transfers" :key="transfer.id" class="hover:bg-gray-50">
                      <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                        #{{ transfer.id }}
                      </td>
                      <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                        {{ formatAccount(transfer.sourceAccount) }}
                      </td>
                      <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                        {{ formatAccount(transfer.destinationAccount) }}
                      </td>
                      <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                        {{ formatCurrency(transfer.transferAmount) }}
                      </td>
                      <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                        {{ formatCurrency(transfer.fee) }}
                      </td>
                      <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                        {{ formatDate(transfer.transferDate) }}
                      </td>
                      <td class="px-6 py-4 whitespace-nowrap">
                        <span 
                          class="inline-flex px-2 py-1 text-xs font-medium rounded-full"
                          :class="getStatusClass(transfer.transferDate)"
                        >
                          {{ getStatusText(transfer.transferDate) }}
                        </span>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>

              <Alert variant="default" class="border-gray-200 bg-gray-50">
                <div>
                  <h4 class="font-medium text-gray-800 mb-2">Resumo</h4>
                  <div class="grid grid-cols-2 gap-4 text-sm">
                    <div>
                      <p class="text-muted-foreground">Total de Transferências:</p>
                      <p class="font-semibold">{{ transfers?.length || 0 }}</p>
                    </div>
                    <div>
                      <p class="text-muted-foreground">Valor Total:</p>
                      <p class="font-semibold">{{ formatCurrency(totalAmount) }}</p>
                    </div>
                  </div>
                </div>
              </Alert>

              <div class="flex justify-end">
                <Button 
                  variant="outline" 
                  @click="refetch"
                  :disabled="isRefetching"
                >
                  {{ isRefetching ? 'Atualizando...' : 'Atualizar' }}
                </Button>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    </main>

    <!-- Toaster for notifications -->
    <Toaster position="top-right" />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

import Card from '@/components/ui/Card.vue'
import CardHeader from '@/components/ui/CardHeader.vue'
import CardTitle from '@/components/ui/CardTitle.vue'
import CardContent from '@/components/ui/CardContent.vue'
import Button from '@/components/ui/Button.vue'
import Alert from '@/components/ui/Alert.vue'
import { Toaster } from 'vue-sonner'

import { useTransfers } from '@/services/queries'

interface Transfer {
  id: number
  sourceAccount: string
  destinationAccount: string
  transferAmount: number
  fee: number
  transferDate: string
  scheduleDate: string
}

const { data: transfers, isLoading, isError, refetch, isRefetching } = useTransfers()

const totalAmount = computed(() => {
  if (!transfers.value) return 0
  return transfers.value.reduce((sum: number, transfer: Transfer) => {
    return sum + transfer.transferAmount + transfer.fee
  }, 0)
})

const formatAccount = (account: string): string => {
  return account.replace(/(\d{4})(\d{3})(\d{3})/, '$1.$2.$3')
}

const formatDate = (dateString: string): string => {
  return new Date(dateString).toLocaleDateString('pt-BR')
}

const formatCurrency = (value: number): string => {
  return new Intl.NumberFormat('pt-BR', {
    style: 'currency',
    currency: 'BRL'
  }).format(value)
}

const getStatusClass = (transferDate: string): string => {
  const today = new Date()
  const transfer = new Date(transferDate)
  
  if (transfer < today) {
    return 'bg-green-100 text-green-800'
  } else if (transfer.toDateString() === today.toDateString()) {
    return 'bg-yellow-100 text-yellow-800'
  } else {
    return 'bg-blue-100 text-blue-800'
  }
}

const getStatusText = (transferDate: string): string => {
  const today = new Date()
  const transfer = new Date(transferDate)
  
  if (transfer < today) {
    return 'Processada'
  } else if (transfer.toDateString() === today.toDateString()) {
    return 'Hoje'
  } else {
    return 'Agendada'
  }
}
</script>
