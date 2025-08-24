<template>
  <NumberFieldRoot v-model="modelValue" v-bind="forwarded" :class="cn('grid gap-2', props.class)">
    <slot :modelValue="modelValue" />
  </NumberFieldRoot>
</template>

<script setup lang="ts">
import type { NumberFieldRootEmits, NumberFieldRootProps } from 'reka-ui'
import { NumberFieldRoot, useForwardPropsEmits } from 'reka-ui'
import { cn } from '@/lib/utils'

const props = defineProps<NumberFieldRootProps & { class?: string }>()
const emits = defineEmits<NumberFieldRootEmits>()
const forwarded = useForwardPropsEmits(props, emits)

const modelValue = defineModel<number>()
</script>
