<template>
  <p 
    v-if="message"
    :class="cn('text-sm font-medium', variant === 'error' ? 'text-destructive' : 'text-muted-foreground', props.class)"
  >
    {{ message }}
  </p>
</template>

<script setup lang="ts">
import { cn } from '@/lib/utils'

export interface FormMessageProps {
  message?: string
  variant?: 'error' | 'description'
  class?: string
}

const props = withDefaults(defineProps<FormMessageProps>(), {
  variant: 'error'
})
</script>
