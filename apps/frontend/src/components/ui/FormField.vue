<template>
  <div :class="cn('grid gap-2', props.class)">
    <slot />
  </div>
</template>

<script setup lang="ts">
import { cn } from '@/lib/utils'

export interface FormFieldProps {
  class?: string
}

const props = withDefaults(defineProps<FormFieldProps>(), {})
</script>
