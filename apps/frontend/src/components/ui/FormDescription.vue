<template>
  <p :class="cn('text-sm text-muted-foreground', props.class)">
    <slot />
  </p>
</template>

<script setup lang="ts">
import { cn } from '@/lib/utils'

export interface FormDescriptionProps {
  class?: string
}

const props = withDefaults(defineProps<FormDescriptionProps>(), {})
</script>
