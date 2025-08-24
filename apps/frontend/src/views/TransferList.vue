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
        <Card class="max-w-full mx-auto">
          <CardHeader class="pb-4 sm:pb-6">
            <div class="flex flex-col sm:flex-row sm:justify-between sm:items-center space-y-4 sm:space-y-0">
              <CardTitle class="text-xl sm:text-2xl text-center sm:text-left">
                Extrato de Transferências
              </CardTitle>
              <Button 
                v-if="transfers?.length"
                variant="outline" 
                @click="downloadCSV"
                class="flex items-center gap-2 self-center sm:self-auto"
                size="sm"
              >
                <DownloadIcon class="h-4 w-4" />
                <span class="hidden sm:inline">Baixar CSV</span>
                <span class="sm:hidden">CSV</span>
              </Button>
            </div>
          </CardHeader>
          
          <CardContent class="px-4 sm:px-6">
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
              <!-- Mobile card layout -->
              <div class="block sm:hidden space-y-4">
                <div 
                  v-for="transfer in transfers" 
                  :key="transfer.id" 
                  class="bg-card border rounded-lg p-4 space-y-3"
                >
                  <div class="flex justify-between items-start">
                    <div>
                      <p class="font-medium text-foreground">#{{ transfer.id }}</p>
                      <span 
                        class="inline-flex px-2 py-1 text-xs font-medium rounded-full"
                        :class="getStatusClass(transfer.transferDate)"
                      >
                        {{ getStatusText(transfer.transferDate) }}
                      </span>
                    </div>
                    <div class="text-right">
                      <p class="font-semibold text-foreground">{{ formatCurrency(transfer.transferAmount) }}</p>
                      <p class="text-xs text-muted-foreground">+ {{ formatCurrency(transfer.fee) }} taxa</p>
                    </div>
                  </div>
                  
                  <div class="space-y-2 text-sm">
                    <div class="flex justify-between">
                      <span class="text-muted-foreground">Origem:</span>
                      <span class="text-foreground">{{ formatAccount(transfer.sourceAccount) }}</span>
                    </div>
                    <div class="flex justify-between">
                      <span class="text-muted-foreground">Destino:</span>
                      <span class="text-foreground">{{ formatAccount(transfer.destinationAccount) }}</span>
                    </div>
                    <div class="flex justify-between">
                      <span class="text-muted-foreground">Data:</span>
                      <span class="text-foreground">{{ formatDate(transfer.transferDate) }}</span>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Desktop table layout -->
              <div class="hidden sm:block overflow-hidden shadow ring-1 ring-black ring-opacity-5 md:rounded-lg">
                <div class="overflow-x-auto">
                  <table class="min-w-full divide-y divide-border">
                    <thead class="bg-muted/50">
                      <tr>
                        <th scope="col" class="px-3 lg:px-6 py-3 text-left text-xs font-medium text-muted-foreground uppercase tracking-wider">
                          ID
                        </th>
                        <th scope="col" class="px-3 lg:px-6 py-3 text-left text-xs font-medium text-muted-foreground uppercase tracking-wider">
                          Conta Origem
                        </th>
                        <th scope="col" class="px-3 lg:px-6 py-3 text-left text-xs font-medium text-muted-foreground uppercase tracking-wider">
                          Conta Destino
                        </th>
                        <th scope="col" class="px-3 lg:px-6 py-3 text-left text-xs font-medium text-muted-foreground uppercase tracking-wider">
                          Valor
                        </th>
                        <th scope="col" class="px-3 lg:px-6 py-3 text-left text-xs font-medium text-muted-foreground uppercase tracking-wider">
                          Taxa
                        </th>
                        <th scope="col" class="px-3 lg:px-6 py-3 text-left text-xs font-medium text-muted-foreground uppercase tracking-wider">
                          Data
                        </th>
                        <th scope="col" class="px-3 lg:px-6 py-3 text-left text-xs font-medium text-muted-foreground uppercase tracking-wider">
                          Status
                        </th>
                      </tr>
                    </thead>
                    <tbody class="bg-card divide-y divide-border">
                      <tr v-for="transfer in transfers" :key="transfer.id" class="hover:bg-muted/50">
                        <td class="px-3 lg:px-6 py-4 whitespace-nowrap text-sm font-medium text-foreground">
                          #{{ transfer.id }}
                        </td>
                        <td class="px-3 lg:px-6 py-4 whitespace-nowrap text-sm text-foreground">
                          {{ formatAccount(transfer.sourceAccount) }}
                        </td>
                        <td class="px-3 lg:px-6 py-4 whitespace-nowrap text-sm text-foreground">
                          {{ formatAccount(transfer.destinationAccount) }}
                        </td>
                        <td class="px-3 lg:px-6 py-4 whitespace-nowrap text-sm text-foreground">
                          {{ formatCurrency(transfer.transferAmount) }}
                        </td>
                        <td class="px-3 lg:px-6 py-4 whitespace-nowrap text-sm text-foreground">
                          {{ formatCurrency(transfer.fee) }}
                        </td>
                        <td class="px-3 lg:px-6 py-4 whitespace-nowrap text-sm text-foreground">
                          {{ formatDate(transfer.transferDate) }}
                        </td>
                        <td class="px-3 lg:px-6 py-4 whitespace-nowrap">
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
              </div>

              <Alert variant="default" class="border-border bg-muted/50">
                  <AlertTitle class="font-medium text-foreground mb-2">Resumo</AlertTitle>
                  <AlertDescription class="grid grid-cols-1 sm:grid-cols-2 gap-2 sm:gap-4 text-sm">
                    <div>
                      <p class="text-muted-foreground">Total de Transferências:</p>
                      <p class="font-semibold text-foreground">{{ transfers?.length || 0 }}</p>
                    </div>
                    <div>
                      <p class="text-muted-foreground">Valor Total:</p>
                      <p class="font-semibold text-foreground">{{ formatCurrency(totalAmount) }}</p>
                    </div>
                  </AlertDescription>
              </Alert>

              <div class="flex justify-center sm:justify-end">
                <Button 
                  variant="outline" 
                  @click="refetch"
                  :disabled="isRefetching"
                  size="sm"
                >
                  {{ isRefetching ? 'Atualizando...' : 'Atualizar' }}
                </Button>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    </main>

    <Toaster position="top-right" />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { DownloadIcon } from 'lucide-vue-next'
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert'
import { Toaster, toast } from 'vue-sonner'

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
  } 
  if (transfer.toDateString() === today.toDateString()) {
    return 'Hoje'
  } 

  return 'Agendada'
}

const downloadCSV = () => {
  if (!transfers.value?.length) {
    toast.error('Nenhuma transferência para exportar')
    return
  }

  try {
    // Define CSV headers
    const headers = [
      'ID',
      'Conta Origem',
      'Conta Destino', 
      'Valor (R$)',
      'Taxa (R$)',
      'Data da Transferência',
      'Data do Agendamento',
      'Status'
    ]

    // Convert transfers to CSV rows
    const csvRows = transfers.value.map((transfer: Transfer) => [
      transfer.id.toString(),
      formatAccount(transfer.sourceAccount),
      formatAccount(transfer.destinationAccount),
      transfer.transferAmount.toFixed(2).replace('.', ','),
      transfer.fee.toFixed(2).replace('.', ','),
      formatDate(transfer.transferDate),
      formatDate(transfer.scheduleDate),
      getStatusText(transfer.transferDate)
    ])

    // Combine headers and rows
    const csvContent = [headers, ...csvRows]
      .map(row => row.map(field => `"${field}"`).join(';'))
      .join('\n')

    // Add BOM for proper encoding in Excel
    const BOM = '\uFEFF'
    const csvWithBOM = BOM + csvContent

    // Create and download the file
    const blob = new Blob([csvWithBOM], { type: 'text/csv;charset=utf-8;' })
    const link = document.createElement('a')
    const url = URL.createObjectURL(blob)
    
    link.setAttribute('href', url)
    link.setAttribute('download', `transferencias_${new Date().toISOString().split('T')[0]}.csv`)
    link.style.visibility = 'hidden'
    
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    
    URL.revokeObjectURL(url)
    
    toast.success('Arquivo CSV baixado com sucesso!')
  } catch (error) {
    console.error('Erro ao gerar CSV:', error)
    toast.error('Erro ao gerar arquivo CSV')
  }
}
</script>
