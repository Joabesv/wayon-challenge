<template>
  <div :class="cn('relative flex items-center', props.class)">
    <NumberFieldDecrement
      :class="cn(
        'absolute left-0.5 size-4 rounded-sm transition-shadow hover:bg-accent hover:text-accent-foreground disabled:pointer-events-none disabled:cursor-not-allowed disabled:opacity-50',
      )"
    >
      <MinusIcon class="size-3" />
    </NumberFieldDecrement>

    <NumberFieldInput
      :class="cn(
        'flex h-10 w-full rounded-md border border-input bg-background py-2 text-center text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50',
        props.class
      )"
    />

    <NumberFieldIncrement
      :class="cn(
        'absolute right-0.5 size-4 rounded-sm transition-shadow hover:bg-accent hover:text-accent-foreground disabled:pointer-events-none disabled:cursor-not-allowed disabled:opacity-50',
      )"
    >
      <PlusIcon class="size-3" />
    </NumberFieldIncrement>
  </div>
</template>

<script setup lang="ts">
import { NumberFieldDecrement, NumberFieldIncrement, NumberFieldInput } from 'reka-ui'
import { MinusIcon, PlusIcon } from 'lucide-vue-next'
import { cn } from '@/lib/utils'

export interface NumberFieldContentProps {
  class?: string
}

const props = withDefaults(defineProps<NumberFieldContentProps>(), {})
</script>
